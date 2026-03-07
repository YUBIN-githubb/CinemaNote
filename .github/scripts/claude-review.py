"""
Claude PR Code Review Script
GitHub Actions에서 실행되어 PR의 diff를 Claude에게 보내고,
리뷰 결과를 PR 코멘트로 작성합니다.
"""

import os
import sys
import json
from pathlib import Path

import anthropic
from github import Github


# ──────────────────────────────────────────────
# 설정
# ──────────────────────────────────────────────
MODEL = "claude-sonnet-4-20250514"
MAX_DIFF_CHARS = 80_000  # diff가 이보다 크면 파일별로 분할 리뷰
MAX_TOKENS = 4096

# 리뷰에서 제외할 파일 패턴
IGNORE_PATTERNS = [
    "package-lock.json",
    "yarn.lock",
    "pnpm-lock.yaml",
    "*.min.js",
    "*.min.css",
    "*.map",
    "*.svg",
    "*.png",
    "*.jpg",
    "*.ico",
    "dist/",
    "build/",
    ".github/workflows/",  # 워크플로우 파일 자체는 리뷰 제외
]

SYSTEM_PROMPT = """\
당신은 시니어 소프트웨어 엔지니어이자 코드 리뷰어입니다.
PR의 diff를 분석하여 건설적이고 실용적인 코드 리뷰를 제공하세요.

## 리뷰 기준
1. **버그 및 논리 오류**: 잠재적 버그, 엣지 케이스 누락, 논리적 오류
2. **보안 취약점**: SQL 인젝션, XSS, 하드코딩된 시크릿, 안전하지 않은 역직렬화 등
3. **성능**: 불필요한 연산, N+1 쿼리, 메모리 누수 가능성
4. **가독성 및 유지보수성**: 네이밍, 함수 분리, 복잡도, 매직넘버
5. **에러 처리**: 누락된 예외 처리, 불충분한 에러 메시지

## 출력 형식
반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.

{
  "summary": "전체 PR에 대한 간략한 요약 (2-3문장)",
  "risk_level": "low | medium | high",
  "comments": [
    {
      "file": "파일경로",
      "line": 라인번호_또는_null,
      "severity": "critical | warning | suggestion | nitpick",
      "comment": "구체적인 리뷰 코멘트"
    }
  ],
  "highlights": ["잘한 점이 있다면 여기에"]
}

## 주의사항
- 사소한 스타일 이슈보다 실질적인 문제에 집중하세요.
- 문제만 지적하지 말고, 가능하면 개선 방안도 제시하세요.
- 코드 예시를 포함할 때는 간결하게 작성하세요.
- 확신이 없는 부분은 "확인 필요" 로 표시하세요.
- 잘 작성된 부분에 대한 칭찬도 포함하세요.
"""


# ──────────────────────────────────────────────
# 유틸리티 함수
# ──────────────────────────────────────────────
def should_ignore(filepath: str) -> bool:
    """리뷰에서 제외할 파일인지 확인"""
    for pattern in IGNORE_PATTERNS:
        if pattern.endswith("/"):
            if filepath.startswith(pattern) or f"/{pattern}" in filepath:
                return True
        elif pattern.startswith("*"):
            if filepath.endswith(pattern[1:]):
                return True
        elif pattern in filepath:
            return True
    return False


def parse_diff(diff_text: str) -> list[dict]:
    """diff를 파일별로 파싱"""
    files = []
    current_file = None
    current_lines = []

    for line in diff_text.split("\n"):
        if line.startswith("diff --git"):
            if current_file:
                files.append({
                    "file": current_file,
                    "diff": "\n".join(current_lines),
                })
            # 파일 경로 추출: "diff --git a/path b/path"
            parts = line.split(" b/")
            current_file = parts[-1] if len(parts) > 1 else "unknown"
            current_lines = [line]
        elif current_file:
            current_lines.append(line)

    if current_file:
        files.append({
            "file": current_file,
            "diff": "\n".join(current_lines),
        })

    return [f for f in files if not should_ignore(f["file"])]


def call_claude(client: anthropic.Anthropic, diff_content: str, pr_context: str) -> dict:
    """Claude API 호출"""
    user_message = f"""{pr_context}

## Diff
```diff
{diff_content}
```
"""
    response = client.messages.create(
        model=MODEL,
        max_tokens=MAX_TOKENS,
        system=SYSTEM_PROMPT,
        messages=[{"role": "user", "content": user_message}],
    )

    raw = response.content[0].text.strip()
    # JSON 블록이 ```json ... ``` 로 감싸진 경우 처리
    if raw.startswith("```"):
        raw = raw.split("\n", 1)[1].rsplit("```", 1)[0]

    return json.loads(raw)


def format_review_body(review: dict) -> str:
    """리뷰 결과를 마크다운으로 포맷팅"""
    risk_emoji = {
        "low": "🟢",
        "medium": "🟡",
        "high": "🔴",
    }
    severity_emoji = {
        "critical": "🚨",
        "warning": "⚠️",
        "suggestion": "💡",
        "nitpick": "🔧",
    }

    lines = []
    lines.append("## 🤖 Claude Code Review\n")
    lines.append(f"**리스크 수준**: {risk_emoji.get(review['risk_level'], '⚪')} {review['risk_level'].upper()}\n")
    lines.append(f"**요약**: {review['summary']}\n")

    # 코멘트를 severity별로 그룹핑
    comments = review.get("comments", [])
    if comments:
        lines.append("---\n### 📝 리뷰 코멘트\n")

        # severity 우선순위 순으로 정렬
        severity_order = {"critical": 0, "warning": 1, "suggestion": 2, "nitpick": 3}
        sorted_comments = sorted(comments, key=lambda c: severity_order.get(c.get("severity", ""), 99))

        for c in sorted_comments:
            emoji = severity_emoji.get(c.get("severity", ""), "📌")
            severity = c.get("severity", "info").upper()
            file_info = f"`{c['file']}`" if c.get("file") else ""
            line_info = f" (L{c['line']})" if c.get("line") else ""

            lines.append(f"#### {emoji} [{severity}] {file_info}{line_info}\n")
            lines.append(f"{c['comment']}\n")

    # 잘한 점
    highlights = review.get("highlights", [])
    if highlights:
        lines.append("---\n### ✨ 잘한 점\n")
        for h in highlights:
            lines.append(f"- {h}")

    lines.append("\n---\n*이 리뷰는 Claude AI에 의해 자동 생성되었습니다. 참고용으로 활용해 주세요.*")

    return "\n".join(lines)


# ──────────────────────────────────────────────
# 메인 실행
# ──────────────────────────────────────────────
def main():
    # 환경 변수 로드
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    github_token = os.environ.get("GITHUB_TOKEN")
    pr_number = int(os.environ.get("PR_NUMBER", "0"))
    repo_name = os.environ.get("REPO_NAME", "")
    pr_title = os.environ.get("PR_TITLE", "")
    pr_body = os.environ.get("PR_BODY", "") or ""

    if not all([api_key, github_token, pr_number, repo_name]):
        print("❌ 필수 환경 변수가 설정되지 않았습니다.")
        sys.exit(1)

    # diff 읽기
    diff_path = Path("pr_diff.txt")
    if not diff_path.exists() or diff_path.stat().st_size == 0:
        print("⏭️ diff가 비어 있습니다. 리뷰를 건너뜁니다.")
        return

    diff_text = diff_path.read_text(encoding="utf-8", errors="replace")

    # PR 컨텍스트 구성
    pr_context = f"## PR 정보\n- **제목**: {pr_title}\n- **설명**: {pr_body[:1000]}"

    # 파일별 파싱 및 필터링
    parsed_files = parse_diff(diff_text)
    if not parsed_files:
        print("⏭️ 리뷰 대상 파일이 없습니다.")
        return

    filtered_diff = "\n".join(f["diff"] for f in parsed_files)
    file_list = ", ".join(f["file"] for f in parsed_files)
    pr_context += f"\n- **변경 파일**: {file_list}"

    print(f"📄 리뷰 대상: {len(parsed_files)}개 파일")
    print(f"📏 Diff 크기: {len(filtered_diff):,} chars")

    # Claude API 호출
    client = anthropic.Anthropic(api_key=api_key)

    if len(filtered_diff) > MAX_DIFF_CHARS:
        # diff가 너무 크면 파일별로 분할하여 리뷰
        print("📦 Diff가 커서 파일별로 분할 리뷰합니다.")
        all_comments = []
        all_highlights = []
        summaries = []

        for file_info in parsed_files:
            if len(file_info["diff"]) < 50:  # 너무 작은 diff는 스킵
                continue
            try:
                result = call_claude(client, file_info["diff"], pr_context)
                all_comments.extend(result.get("comments", []))
                all_highlights.extend(result.get("highlights", []))
                summaries.append(result.get("summary", ""))
            except Exception as e:
                print(f"⚠️ {file_info['file']} 리뷰 실패: {e}")

        review = {
            "summary": " ".join(s for s in summaries if s)[:500],
            "risk_level": "medium",
            "comments": all_comments,
            "highlights": all_highlights,
        }
    else:
        # 한 번에 리뷰
        print("🔍 Claude에게 리뷰를 요청합니다...")
        review = call_claude(client, filtered_diff, pr_context)

    # 리뷰 결과 포맷팅
    review_body = format_review_body(review)
    print(f"\n{'='*50}")
    print(review_body)
    print(f"{'='*50}\n")

    # GitHub PR에 코멘트 작성
    gh = Github(github_token)
    repo = gh.get_repo(repo_name)
    pr = repo.get_pull(pr_number)

    # 기존 봇 코멘트가 있으면 업데이트, 없으면 새로 생성
    bot_comment = None
    for comment in pr.get_issue_comments():
        if "🤖 Claude Code Review" in comment.body:
            bot_comment = comment
            break

    if bot_comment:
        bot_comment.edit(review_body)
        print("✅ 기존 리뷰 코멘트를 업데이트했습니다.")
    else:
        pr.create_issue_comment(review_body)
        print("✅ 새 리뷰 코멘트를 작성했습니다.")


if __name__ == "__main__":
    main()