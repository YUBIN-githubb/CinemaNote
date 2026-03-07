"""
Claude PR Discussion Script
PR 코멘트에서 @claude 멘션 시 대화 맥락을 수집하고
Claude와 토론할 수 있게 해주는 스크립트입니다.
"""

import os
import sys
import json
from pathlib import Path
from datetime import datetime

import anthropic
from github import Github


# ──────────────────────────────────────────────
# 설정
# ──────────────────────────────────────────────
MODEL = "claude-sonnet-4-20250514"
MAX_TOKENS = 4096
MAX_CONVERSATION_TURNS = 20  # 최대 대화 턴 수 (너무 길어지는 것 방지)
CLAUDE_BOT_MARKER = "🤖"  # Claude 코멘트 식별자

SYSTEM_PROMPT = """\
당신은 시니어 소프트웨어 엔지니어이자 멘토입니다.
개발자와 PR 코드에 대해 토론하며, 개발자의 성장을 돕는 것이 목표입니다.

## 토론 원칙

1. **소크라테스식 대화**: 답을 바로 주기보다, 개발자가 스스로 생각할 수 있도록
   좋은 질문을 던지세요. 단, 개발자가 명확한 답을 원할 때는 직접적으로 답하세요.

2. **맥락 기반 설명**: 단순히 "이렇게 하세요"가 아니라, WHY(왜 그래야 하는지)를
   함께 설명하세요. 원리를 이해하면 비슷한 상황에서 스스로 판단할 수 있습니다.

3. **트레이드오프 논의**: 하나의 정답만 제시하지 마세요. 여러 접근법의 장단점을
   비교하고, 현재 상황에서 왜 특정 방법이 더 나은지 함께 생각해보세요.

4. **실전 연결**: 이론적인 설명에 그치지 않고, 실제 코드에서 어떻게 적용되는지
   구체적인 예시를 들어주세요.

5. **긍정적 강화**: 개발자가 좋은 질문을 하거나 좋은 관점을 제시하면
   적극적으로 인정하고 격려하세요.

6. **깊이 조절**: 개발자의 질문 수준에 맞춰 설명의 깊이를 조절하세요.
   초보 질문에는 기초부터, 심화 질문에는 더 깊은 내용을 다루세요.

## 응답 형식
- 마크다운으로 작성하세요.
- 코드 예시가 필요하면 포함하되, 간결하게 작성하세요.
- 응답 끝에 개발자가 더 생각해볼 수 있는 후속 질문을 1개 던지세요.
- 지나치게 길지 않게, 핵심에 집중하세요.
"""


# ──────────────────────────────────────────────
# 대화 맥락 수집
# ──────────────────────────────────────────────
def collect_conversation_thread(pr, trigger_comment_id: int) -> list[dict]:
    """
    PR의 코멘트들에서 Claude와의 대화 스레드를 수집합니다.
    시간순으로 정렬하여 대화 맥락을 구성합니다.
    """
    all_comments = list(pr.get_issue_comments())
    all_comments.sort(key=lambda c: c.created_at)

    # Claude의 첫 리뷰 코멘트 찾기
    claude_review = None
    for c in all_comments:
        if CLAUDE_BOT_MARKER in (c.body or "") and "Claude Code Review" in (c.body or ""):
            claude_review = c
            break

    # 대화 스레드 구성
    thread = []

    # 1. 원본 리뷰가 있으면 포함
    if claude_review:
        thread.append({
            "role": "assistant",
            "type": "review",
            "user": "claude",
            "body": claude_review.body,
            "time": claude_review.created_at.isoformat(),
        })

    # 2. @claude가 포함된 코멘트와 Claude 응답을 시간순으로 수집
    for c in all_comments:
        if c.id == (claude_review.id if claude_review else None):
            continue  # 원본 리뷰는 이미 추가됨

        is_claude_reply = CLAUDE_BOT_MARKER in (c.body or "") and "Claude" in (c.body or "")

        if is_claude_reply:
            thread.append({
                "role": "assistant",
                "type": "reply",
                "user": "claude",
                "body": c.body,
                "time": c.created_at.isoformat(),
            })
        elif "@claude" in (c.body or "").lower():
            thread.append({
                "role": "user",
                "type": "question",
                "user": c.user.login,
                "body": c.body,
                "time": c.created_at.isoformat(),
            })

    # 트리거 코멘트가 스레드에 없으면 추가
    trigger_in_thread = any(
        c.get("body") == os.environ.get("COMMENT_BODY") for c in thread if c["role"] == "user"
    )
    if not trigger_in_thread:
        thread.append({
            "role": "user",
            "type": "question",
            "user": os.environ.get("COMMENT_USER", "developer"),
            "body": os.environ.get("COMMENT_BODY", ""),
            "time": datetime.now().isoformat(),
        })

    return thread[-MAX_CONVERSATION_TURNS:]  # 최근 N개만


def build_messages(thread: list[dict], diff_summary: str, pr_title: str) -> list[dict]:
    """대화 스레드를 Claude API 메시지 형식으로 변환"""
    messages = []

    # 첫 메시지에 PR 컨텍스트 포함
    context = f"[PR 컨텍스트]\n제목: {pr_title}\n\n변경 사항 요약:\n```diff\n{diff_summary[:30000]}\n```\n\n"

    for i, entry in enumerate(thread):
        body = entry["body"]
        user = entry["user"]

        # @claude 멘션 제거 (불필요한 노이즈)
        clean_body = body.replace("@claude", "").replace("@Claude", "").strip()

        if entry["role"] == "user":
            content = f"[{user}]: {clean_body}"
            if i == 0:
                content = context + content
            messages.append({"role": "user", "content": content})
        else:
            # Claude의 이전 응답
            messages.append({"role": "assistant", "content": clean_body})

    # 메시지가 비어있거나 마지막이 assistant면 user 메시지 추가
    if not messages or messages[-1]["role"] == "assistant":
        comment_body = os.environ.get("COMMENT_BODY", "")
        clean = comment_body.replace("@claude", "").replace("@Claude", "").strip()
        user = os.environ.get("COMMENT_USER", "developer")
        content = f"[{user}]: {clean}"
        if not messages:
            content = context + content
        messages.append({"role": "user", "content": content})

    return messages


# ──────────────────────────────────────────────
# 메인 실행
# ──────────────────────────────────────────────
def main():
    # 환경 변수
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    github_token = os.environ.get("GITHUB_TOKEN")
    pr_number = int(os.environ.get("PR_NUMBER", "0"))
    repo_name = os.environ.get("REPO_NAME", "")
    comment_user = os.environ.get("COMMENT_USER", "")

    if not all([api_key, github_token, pr_number, repo_name]):
        print("❌ 필수 환경 변수가 설정되지 않았습니다.")
        sys.exit(1)

    # GitHub 연결
    gh = Github(github_token)
    repo = gh.get_repo(repo_name)
    pr = repo.get_pull(pr_number)

    print(f"💬 {comment_user}님의 질문에 응답합니다 (PR #{pr_number})")

    # Diff 읽기
    diff_path = Path("pr_diff.txt")
    diff_text = ""
    if diff_path.exists():
        diff_text = diff_path.read_text(encoding="utf-8", errors="replace")

    # 대화 스레드 수집
    comment_id = int(os.environ.get("COMMENT_ID", "0"))
    thread = collect_conversation_thread(pr, comment_id)

    print(f"📜 대화 맥락: {len(thread)}개 메시지")
    for t in thread:
        role_icon = "🧑" if t["role"] == "user" else "🤖"
        print(f"  {role_icon} [{t['user']}] {t['body'][:80]}...")

    # Claude API 메시지 구성
    messages = build_messages(thread, diff_text, pr.title)

    # Claude 호출
    client = anthropic.Anthropic(api_key=api_key)

    print("🤔 Claude가 생각 중...")
    response = client.messages.create(
        model=MODEL,
        max_tokens=MAX_TOKENS,
        system=SYSTEM_PROMPT,
        messages=messages,
    )

    reply_text = response.content[0].text.strip()

    # 응답 포맷팅
    formatted_reply = f"{CLAUDE_BOT_MARKER} **Claude**\n\n{reply_text}"
    formatted_reply += f"\n\n---\n*토론을 계속하려면 `@claude`를 멘션하며 답글을 달아주세요.*"

    # PR에 답글 작성
    pr.create_issue_comment(formatted_reply)
    print(f"✅ 답글을 작성했습니다.")
    print(f"\n{'='*50}")
    print(formatted_reply[:500])
    print(f"{'='*50}")


if __name__ == "__main__":
    main()