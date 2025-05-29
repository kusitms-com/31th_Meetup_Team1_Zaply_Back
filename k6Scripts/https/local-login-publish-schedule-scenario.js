import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { __ITER } from 'k6/execution';

export let options = {
    vus: 10,
    duration: '30s',
};

export function setup() {
    // 1) 로그인해서 accessToken을 가져옴
    const loginRes = http.post(
        'http://localhost:8080/v1/auth/sign-in',
        JSON.stringify({ email: 'zaply123@gmail.com', password: 'password123' }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    check(loginRes, {'login status 200': (r) => r.status === 200,});

    const token = loginRes.json().data.tokenResponse.accessToken;

    // 2) 빈 프로젝트 생성 (한 번만)
    const authHeaders = {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
        },
    };
    const createRes = http.post(
        'http://localhost:8080/v1/project/create',
        '',    // 빈 바디
        authHeaders
    );
    check(createRes, { 'create status 200': (r) => r.status === 200 });
    const projectId = createRes.json().data;  // 숫자형 ID

    return { token, projectId };
}

export default function (data) {
    const { token, projectId } = data;
    const authHeaders = {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
        },
    };

    // 보호된 엔드포인트 호출 예시
    group('1) Member accounts 조회', () => {
        const res = http.get('http://localhost:8080/v1/member/accounts', authHeaders);
        check(res, { 'accounts 200': (r) => r.status === 200 });
    });

    group('2) 페이스북 캐러셀 미디어 발행', () => {
        const payload = {
            mediaType: 'IMAGE',
            media: [
                'https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp',
                'https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp',
            ],
            text: '미디어 텍스트1',
            scheduledAt: '2025-06-20T07:09',
        };
        const carouselRes = http.post(
            `http://localhost:8080/v1/posting/facebook/${projectId}/carousel`,
            JSON.stringify(payload),
            authHeaders
        );
        check(carouselRes, {
            'carousel status 200': (r) => r.status === 200,
            // 혹은 201 응답이라면 (r) => r.status === 201
        });
    });

    group('3) 페이스북 캐러셀 발행 수정(reschedule)', () => {
        const base = new Date(Date.UTC(2025, 5, 30, 7, 23, 0));
        const runDate = new Date(base.getTime() + __ITER * 60 * 1000);
        const scheduledAt = runDate.toISOString().substring(0,16);

        const reschedulePayload = {
            mediaType: 'IMAGE',
            media: [
                'https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp',
                'https://zaply-landing.vercel.app/assets/images/ZaplyLanding.webp',
            ],
            text: '수정된 미디어 텍스트',
            scheduledAt: scheduledAt,
        };
        // PUT URL에 query로 snsType=FACEBOOK 붙이기
        const rescheduleRes = http.put(
            // `https://api.zapply.site/v1/posting/${postingId}/carousel/reschedule?snsType=FACEBOOK`,
            `http://localhost:8080/v1/posting/1/carousel/reschedule?snsType=FACEBOOK`,
            JSON.stringify(reschedulePayload),
            authHeaders
        );
        check(rescheduleRes, {
            'reschedule status 200': (r) => r.status === 200,
        });
    });
}

