package com.potatoes.Naengu;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("운영 환경변수(JWT_SECRET_KEY 등) 없이는 전체 컨텍스트 로딩 불가 — 서버에서만 실행")
class NaenguApplicationTests {

	@Test
	void contextLoads() {
	}

}
