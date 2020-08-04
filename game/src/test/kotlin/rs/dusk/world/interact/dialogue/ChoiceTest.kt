package rs.dusk.world.interact.dialogue

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.action.Contexts
import rs.dusk.engine.client.ui.open

internal class ChoiceTest : DialogueTest() {

    @TestFactory
    fun `Send choice lines`() = arrayOf(
        """
            One
            Two
        """ to "multi2",
        "One\nTwo\nThree" to "multi3",
        "One\nTwo\nThree\nFour" to "multi4",
        "One\nTwo\nThree\nFour\nFive" to "multi5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start(context) {
                choice(text = text)
            }
            runBlocking(Contexts.Game) {
                verify {
                    player.open(expected)
                    for((index, line) in text.trimIndent().lines().withIndex()) {
                        interfaces.sendText(expected, "line${index + 1}", line)
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Send multi line choice lines`() = arrayOf(
        """
            One
            Two<br>Three
        """ to "multi2_chat",
        "One\nTwo\nThree<br>Four" to "multi3_chat",
        "One\nTwo<br>Five\nThree\nFour" to "multi4_chat",
        "One\nTwo\nThree\nFour<br>Six\nFive" to "multi5_chat"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start(context) {
                choice(text = text)
            }
            runBlocking(Contexts.Game) {
                verify {
                    player.open(expected)
                    for ((index, line) in text.trimIndent().lines().withIndex()) {
                        interfaces.sendText(expected, "line${index + 1}", line)
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Send multi line title choice lines`() = arrayOf(
        """
            One
            Two
        """ to "multi_var2",
        "One\nTwo\nThree" to "multi_var3",
        "One\nTwo\nThree\nFour" to "multi_var4",
        "One\nTwo\nThree\nFour\nFive" to "multi_var5"
    ).map { (text, expected) ->
        dynamicTest("Text '$text' expected $expected") {
            manager.start(context) {
                choice(text = text, title = "First<br>Second")
            }
            runBlocking(Contexts.Game) {
                verify {
                    player.open(expected)
                    for ((index, line) in text.trimIndent().lines().withIndex()) {
                        interfaces.sendText(expected, "line${index + 1}", line)
                    }
                }
            }
        }
    }

    @Test
    fun `Sending six or more lines is ignored`() {
        manager.start(context) {
            choice(text = "\nOne\nTwo\nThree\nFour\nFive\nSix")
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                player.open(any())
            }
        }
    }

    @Test
    fun `Sending less than two lines is ignored`() {
        manager.start(context) {
            choice(text = "One line")
        }
        runBlocking(Contexts.Game) {
            verify(exactly = 0) {
                player.open(any())
            }
        }
    }

    @Test
    fun `Send no title`() {
        manager.start(context) {
            choice(text = "Yes\nNo", title = null)
        }
        runBlocking(Contexts.Game) {
            verify {
                player.open("multi2")
            }
            verify(exactly = 0) {
                interfaces.sendText("multi2", "title", any())
            }
        }
    }

    @Test
    fun `Choice not sent if interface not opened`() {
        every { player.open("multi2") } returns false
        coEvery { context.await<Int>(any()) } returns 0
        manager.start(context) {
            choice(text = "Yes\nNo")
        }
        runBlocking(Contexts.Game) {
            coVerify(exactly = 0) {
                context.await<Any>(any())
                interfaces.sendText("multi2", "line1", "Yes")
                interfaces.sendText("multi2", "line2", "No")
            }
        }
    }

    @Test
    fun `Send choice`() {
        coEvery { context.await<Int>(any()) } returns 0
        manager.start(context) {
            choice(text = "Yes\nNo")
        }
        runBlocking(Contexts.Game) {
            coVerify {
                context.await<Int>("choice")
                interfaces.sendText("multi2", "line1", "Yes")
                interfaces.sendText("multi2", "line2", "No")
            }
        }
    }
}