import http from 'k6/http';
import { check } from 'k6';

export let options = {
    vus: 10,
    duration: '30s',
};

export function setup() {
    // 1) 로그인해서 accessToken을 가져옴
    const loginRes = http.post(
        'https://api.zapply.site/v1/auth/sign-in',
        JSON.stringify({ email: 'zaply123@gmail.com', password: 'password123' }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    check(loginRes, {'login status 200': (r) => r.status === 200,});

    const body = loginRes.json();
    const token = body.data.tokenResponse.accessToken;
    return token;
}

export default function (token) {
    // 2) setup()에서 반환된 token을 모든 요청에 사용
    const authHeaders = {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
        },
    };

    // 보호된 엔드포인트 호출 예시
    const res = http.get('https://api.zapply.site/v1/member/accounts', authHeaders);
    check(res, { 'status is 200': (r) => r.status === 200 });
}
