package com.issueissyu.fe.core.text

/**
 * pinContent의 줄바꿈을 화면 표시용으로 정규화한다.
 * - JSON 파싱 후 실제 개행(U+000A)은 그대로 유지
 * - literal `\n`(`\` + `n`)이 들어온 경우 실제 개행으로 변환
 */
fun String.decodePinContentNewlines(): String =
    replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace("\\r", "\n")
