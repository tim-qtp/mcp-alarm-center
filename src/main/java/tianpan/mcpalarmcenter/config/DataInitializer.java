package tianpan.mcpalarmcenter.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // 已有数据，无需初始化测试数据
    }
} 