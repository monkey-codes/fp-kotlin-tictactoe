package codes.monkey.tictactoe

class TestInterpreterContext(vararg inputValues: String) : InterpreterContext {
  private val inputs = inputValues.toMutableList()
  val outputs: MutableList<String?> = mutableListOf()

  override fun printMessage(message: Any?) {
    outputs.add(message?.toString())
  }

  override fun readLineFromInput(): String? {
    return inputs.removeFirstOrNull()
  }
}
