package tech.dzolotov.mycounter1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.presenter.presenterOf
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.android.parcel.Parcelize

@Composable
fun MainContent(navigator: Navigator, backstack: SaveableBackStack): CircuitConfig {
    val circuitConfig = CircuitConfig.Builder()
        .addPresenterFactory(CounterPresenterFactory())
        .addUiFactory(CounterUiFactory(navigator = navigator))
        .build()
    CircuitCompositionLocals(circuitConfig = circuitConfig) {
        NavigableCircuitContent(navigator = navigator, backstack = backstack)
    }
    return circuitConfig
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val backstack = rememberSaveableBackStack {
                this.push(HomeScreen)
            }
            val navigator = rememberCircuitNavigator(backstack)
            MainContent(navigator = navigator, backstack = backstack)
        }
    }
}

class CounterUiFactory(val navigator: Navigator) : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is CounterScreen -> ui<CounterState> { state, modifier -> Counter(state = state) }
            is HomeScreen -> ui<HomeState> { state, modifier -> Home(navigator = navigator) }
            else -> null
        }
    }
}

class CounterPresenterFactory : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext
    ): Presenter<*>? {
        return when (screen) {
            is CounterScreen -> presenterOf { CounterPresenter(screen.title) }
            is HomeScreen -> presenterOf { HomePresenter() }
            else -> null
        }
    }
}

//возможные события от экрана
sealed interface CounterEvent : CircuitUiEvent {
    object Increment : CounterEvent
    object OtherEvent : CounterEvent
}

data class CounterState(
    val title: String,
    val counter: Int,
    val eventSink: (CounterEvent) -> Unit
) :
    CircuitUiState

@Parcelize
class CounterScreen(val title: String) : Screen

@Parcelize
object HomeScreen : Screen

//пустое состояние
class HomeState : CircuitUiState

//здесь нет состояния и его изменения, поэтому просто возвращаем
//состояние по умолчанию
@Composable
fun HomePresenter(): HomeState = HomeState()

@Composable
fun Home(navigator: Navigator) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Text("Welcome to our Circuit counter", modifier = Modifier.semantics {
        testTag = "Welcome Label"
    })
    LazyColumn {
        items(5) {
            Text("Counter $it", modifier = Modifier.clickable {
                navigator.goTo(CounterScreen("Counter $it"))
            }.semantics {
                testTag = "Counter $it"
            })
        }
    }
}

@Composable
fun CounterPresenter(title: String): CounterState {
    var counter by remember { mutableStateOf(0) }

    return CounterState(title, counter) { event ->
        when (event) {
            CounterEvent.Increment -> counter++
            else -> println("Unknown event")
        }
    }
}

@Composable
fun Counter(state: CounterState) {
    Surface {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Page title is ${state.title}")
            Text("Counter value is ${state.counter}", modifier = Modifier.semantics {
                testTag = "Counter"
            })
            Button({
                state.eventSink(CounterEvent.Increment)
            }, modifier = Modifier.semantics {
                testTag = "Increment"
            }) {
                Text("Increment")
            }
        }
    }
}