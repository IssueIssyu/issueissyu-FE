package com.issueissyu.fe.ui.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.issueissyu.fe.ui.components.IssueissyuTopAppBar
import com.issueissyu.fe.ui.theme.IssueTypo
import com.issueissyu.fe.ui.theme.White

@Composable
fun TermDetailScreen(
    termsType: TermsType,
    onBackClick: () -> Unit = {}
){
    val title = when(termsType){
        TermsType.SERVICE -> "서비스 이용약관 동의"
        TermsType.PRIVACY -> "개인정보 수집 및 이용 동의"
        TermsType.LOCATION -> "위치기반 서비스 이용약관 동의"
    }

    val content = when (termsType){
        TermsType.SERVICE -> getServiceContent()
        TermsType.PRIVACY -> getPrivacyContent()
        TermsType.LOCATION -> getLocationContent()
    }

    Scaffold(
        topBar = {
            IssueissyuTopAppBar(
                onBackClick = onBackClick,
                titleText = title
            )
        },
        containerColor = White
    ){  paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(31.dp)
                .verticalScroll(rememberScrollState())
        ){
            Text(
                text = content,
                style = IssueTypo.Regular16
            )
        }

    }
}

private fun getServiceContent(): String {
    return """
            제1조 (목적)
            본 약관은 ○○(이하 "회사")가 제공하는
            위치 기반 커뮤니티 서비스(이하 "서비스")의 이용과
            관련하여 회사와 이용자 간의 권리, 의무 및 책임사항을
            규정함을 목적으로 합니다.

            제2조 (정의)
            1. "서비스"란 회사가 제공하는
            지도 기반 정보 공유 및 커뮤니티 플랫폼을 의미합니다.
            2. "이용자"란 본 약관에 따라 서비스를 이용하는
            회원 및 비회원을 의미합니다.
            3. "회원"이란 서비스에 가입하여 계정을 생성한
            이용자를 의미합니다.
            4. "콘텐츠"란 이용자가 서비스에 게시한
            글, 이미지, 위치 정보, 댓글 등을 의미합니다.

            제3조 (약관의 효력 및 변경)
            1. 본 약관은 서비스 화면에 게시함으로써
            효력이 발생합니다.
            2. 회사는 관련 법령을 위반하지 않는 범위에서
            약관을 변경할 수 있습니다.
            3. 변경된 약관은 공지 후 일정 기간이 경과하면
            효력이 발생합니다.

            제4조 (서비스의 제공)
            회사는 다음과 같은 서비스를 제공합니다.
            1. 지도 기반 위치 정보 제공
            2. 사용자 콘텐츠(핀, 게시글 등) 등록 및 조회
            3. 커뮤니티 및 정보 공유 기능
            4. 기타 회사가 추가 개발하거나 제공하는 서비스

            제5조 (회원가입 및 계정 관리)
            1. 이용자는 약관에 동의하고 회원가입 절차를 완료함으로써
            회원이 됩니다.
            2. 회원은 본인의 계정을 직접 관리해야 하며,
            계정 정보 관리 책임은 회원에게 있습니다.
            3. 타인의 계정을 도용하거나 부정 사용해서는
            안 됩니다.

            제6조 (이용자의 의무)
            이용자는 다음 행위를 해서는 안 됩니다.
            1. 허위 정보 또는 잘못된 위치 정보 등록
            2. 타인을 비방하거나 불쾌감을 주는 행위
            3. 불법 콘텐츠 또는 음란물 게시
            4. 광고, 도배, 스팸 행위
            5. 서비스의 정상적인 운영을 방해하는 행위
            6. 기타 법령에 위반되는 행위

            제7조 (콘텐츠의 권리 및 책임)
            1. 이용자가 게시한 콘텐츠의 저작권은
            해당 이용자에게 귀속됩니다.
            2. 회사는 서비스 운영, 개선 및 홍보를 위해
            콘텐츠를 사용할 수 있습니다.
            3. 이용자가 게시한 콘텐츠에 대한 책임은
            해당 이용자에게 있습니다.
            4. 회사는 정책 위반 콘텐츠를 사전 통보 없이
            삭제할 수 있습니다.

            제8조 (서비스의 변경 및 중단)
            1. 회사는 서비스의 일부 또는 전부를
            변경하거나 중단할 수 있습니다.
            2. 서비스 변경 시 사전에 공지합니다.
            3. 기술적 문제나 운영상 필요에 따라
            서비스가 일시 중단될 수 있습니다.

            제9조 (회원 탈퇴 및 이용 제한)
            1. 회원은 언제든지 탈퇴할 수 있습니다.
            2. 회사는 다음 경우 회원의 이용을 제한할 수 있습니다.
            - 약관 위반
            - 불법 행위
            - 서비스 운영 방해

            제10조 (면책 조항)
            1. 회사는 이용자가 게시한 정보의 정확성을 보장하지 않습니다.
            2. 이용자 간 발생한 분쟁에 대해 회사는 책임을 지지 않습니다.
            3. 회사는 천재지변, 시스템 장애 등 불가항력적 사유에 대해 책임을 지지 않습니다.

            제11조 (위치정보 이용)
            1. 서비스는 위치 기반 기능 제공을 위해 이용자의 위치 정보를 사용할 수 있습니다.
            2. 이용자는 언제든지 위치정보 사용을 거부할 수 있습니다.

            제12조 (개인정보 보호) 개인정보 처리에 관한 사항은 별도의 개인정보 처리방침에 따릅니다.

            제13조 (준거법 및 관할) 본 약관은 대한민국 법률에 따라 해석되며, 서비스와 관련된 분쟁은 회사의 본사 소재지를 관할하는 법원을 따릅니다.
        """.trimIndent()
}
private fun getPrivacyContent(): String {
    return """
            제1조 (개인정보의 수집 항목)
            회사는 다음의 개인정보를 수집할 수 있습니다.
            회원가입 시
            아이디, 비밀번호, 이메일, 휴대전화번호
            서비스 이용 시
            위치 정보, 게시글, 이미지, 댓글
            자동 수집 정보
            접속 로그, 기기 정보, 쿠키

            제2조 (개인정보의 수집 및 이용 목적)
            회사는 다음의 목적을 위해 개인정보를 이용합니다.
            회원 관리 및 본인 확인
            서비스 제공 및 기능 개선
            위치 기반 서비스 제공
            고객 문의 대응
            부정 이용 방지

            제3조 (개인정보의 보유 및 이용 기간)
            회원 탈퇴 시 개인정보는 즉시 삭제됩니다.
            단, 관련 법령에 따라 일정 기간 보관될 수 있습니다.
            예:
            계약/결제 기록: 5년
            접속 로그: 3개월

            제4조 (개인정보의 제3자 제공)
            회사는 이용자의 개인정보를 원칙적으로 외부에 제공하지 않습니다.  단, 다음의 경우는 예외로 합니다.
            이용자의 동의가 있는 경우
            법령에 따른 요청이 있는 경우

            제5조 (개인정보의 처리 위탁)
            회사는 서비스 운영을 위해 일부 업무를 외부에 위탁할 수 있습니다.
            예:
            클라우드 서버 (AWS 등)
            문자 발송 서비스

            제6조 (이용자의 권리)
            이용자는 언제든지 다음 권리를 행사할 수 있습니다.
            개인정보 열람
            수정 및 삭제 요청
            처리 정지 요청

            제7조 (개인정보 보호 조치)
            회사는 개인정보 보호를 위해 다음과 같은 조치를 취합니다.
            암호화 저장
            접근 권한 제한
            보안 시스템 운영

            제8조 (개인정보 보호책임자)
            담당자: ○○○
            이메일: ○○○@example.com

            제9조 (정책 변경)
            본 방침은 변경될 수 있으며, 변경 시 공지합니다.
        """.trimIndent()
}
private fun getLocationContent():String {
    return """
            제1조 (목적)
            본 약관은 위치정보를 활용한 서비스 제공에 필요한 사항을 규정합니다.

            제2조 (위치정보의 수집)
            회사는 다음과 같은 방법으로 위치정보를 수집할 수 있습니다.
            GPS
            네트워크 기반 위치 정보

            제3조 (위치정보 이용 목적)
            지도 기반 콘텐츠 표시
            주변 정보 제공
            사용자 간 위치 기반 커뮤니티 기능

            제4조 (위치정보 보유 기간)
            위치정보는 서비스 제공 목적 달성 후 즉시 삭제됩니다.

            제5조 (이용자의 권리)
            이용자는 언제든지 위치정보 제공을 거부할 수 있습니다.

            제6조 (위치정보 보호)
            회사는 위치정보를 안전하게 관리합니다.
        """.trimIndent()
}






@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermDetailScreenPreview(){
    TermDetailScreen(
        TermsType.SERVICE
    )
}