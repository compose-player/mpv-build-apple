import fr.composeplayer.builds.mpv.Main
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test

class MainTest {

  @Test
  fun testMainDoesNotFail() {
    assertDoesNotThrow {
      Main.main( emptyArray() )
    }
  }

}