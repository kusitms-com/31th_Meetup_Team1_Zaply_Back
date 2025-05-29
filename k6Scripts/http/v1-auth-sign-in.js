import http from 'k6/http';
import { check } from 'k6';

export let options = {
    vus: 10,           // 동시 사용자 수
    duration: '30s',   // 테스트 지속 시간
};

export default function () {
    const url = 'http://3.26.64.180:80' +
        '';
    const payload = JSON.stringify({
        email: 'zaply123@gmail.com',
        password: 'password123',
    });
    const params = { headers: { 'Content-Type': 'application/json' } };

    let res = http.post(url, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200
    });
}
