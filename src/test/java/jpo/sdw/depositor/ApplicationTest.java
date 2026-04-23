package jpo.sdw.depositor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

public class ApplicationTest {

   @Test
   public void test() {
      try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
         Application.main(new String[] { "testArg" });
         mocked.verify(() -> SpringApplication.run(any(Class.class), any(String[].class)));
      }
   }

}
