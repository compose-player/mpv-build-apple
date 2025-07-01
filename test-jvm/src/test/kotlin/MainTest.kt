import fr.composeplayer.builds.mpv.Main
import kotlin.test.Test
import kotlin.test.assertEquals

class MainTest {

  @Test
  fun testMainDoesNotFail() {
    assertEquals(
      expected = 131077L,
      actual = Main.getMpvVersion()
    )
  }

}