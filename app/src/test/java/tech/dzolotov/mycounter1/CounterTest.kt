package tech.dzolotov.mycounter1

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.push
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CounterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val fakeNavigator = FakeNavigator()

    @Test
    fun testHome() = runTest {
        composeTestRule.setContent {
            val backStack = rememberSaveableBackStack {
                push(HomeScreen)
            }
            MainContent(navigator = fakeNavigator, backstack = backStack)
        }
        composeTestRule.onNodeWithTag("Welcome Label").assertExists()
        //проверяем навигацию
        composeTestRule.onNodeWithTag("Counter 0").performClick()
        val newScreen = fakeNavigator.awaitNextScreen()
        assert(newScreen is CounterScreen)
        assert((newScreen as CounterScreen).title == "Counter 0")
    }

    @Test
    fun unitTestCounter() = runTest {

        //сохраним конфигурацию (будет нужна для получения презентера)
        lateinit var circuitConfig: CircuitConfig
        val screen = CounterScreen("Counter 0")

        composeTestRule.setContent {
            val backStack = rememberSaveableBackStack {
                push(screen)
            }
            circuitConfig = MainContent(navigator = fakeNavigator, backstack = backStack)
        }

        val presenter = circuitConfig.presenter(screen, fakeNavigator)
        //тестируем презентер
        presenter?.test {
            //убеждаемся что вначале там 0 и отправляем событие
            awaitItem().run {
                assert((this as CounterState).counter==0)
                eventSink(CounterEvent.Increment)
            }
            //проверяем, что счетчик увеличился
            assert((awaitItem() as CounterState).counter==1)
        }
    }

    //проверка интерфейса
    @Test
    fun testCounter() = runTest {

        //создаем начальный экран
        val screen = CounterScreen("Counter 0")
        //инициализируем compose
        composeTestRule.setContent {
            val backStack = rememberSaveableBackStack {
                push(screen)
            }
            MainContent(navigator = fakeNavigator, backstack = backStack)
        }
        //обычным образом взаимодействуем с узлами на экране
        val node = composeTestRule.onNodeWithTag("Counter")
        node.assertExists().assertIsDisplayed()
        node.assertTextContains("0", substring = true)
        //нажимаем на кнопку
        composeTestRule.onNodeWithTag("Increment").performClick()
        //и проверяем увеличение счетчика
        composeTestRule.onNodeWithTag("Counter").assertTextContains("1", substring = true)
    }

    @Test
    fun screenShot() {
        composeTestRule.setContent {
            val backStack = rememberSaveableBackStack {
                push(HomeScreen)
            }
            MainContent(navigator = fakeNavigator, backstack = backStack)
        }
        composeTestRule.onNodeWithTag("Welcome Label").captureRoboImage("build/welcome_message.png")
    }
}
//
//
//    var navigator = FakeNavigator()
//
//    @Test
//    fun testCounter() = runTest {
//        compose.setContent {
//            val backstack = SaveableBackStack()
//            backstack.push(HomeScreen)
//            NavigableCircuitContent(navigator = navigator, backstack = backstack)
//        }
//    }
//}