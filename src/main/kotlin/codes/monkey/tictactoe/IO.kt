package codes.monkey.tictactoe

sealed class IO<A> {

  companion object {
    // Lift any type into IO monad
    fun <A> unit(a: A): IO<A> = Suspend { a }
  }

  fun <B> flatMap(f: (A) -> IO<B>): IO<B> = FlatMap<A, B>(this, f)

  fun <B> map(f: (A) -> B): IO<B> = flatMap { a -> Return(f(a)) }

  // combinators

  fun doWhile(cond: (A) -> IO<Boolean>): IO<Unit> = flatMap { a ->
    cond(a).flatMap { ok: Boolean -> if (ok) this.doWhile(cond) else unit(Unit) }
  }

  fun <B> forever(): IO<B> {
    val t: IO<B> by lazy { forever<B>() }
    return flatMap { t }
  }

  infix fun <B> assoc(io: IO<B>): IO<Pair<A, B>> = flatMap { a -> io.map { b -> a to b } }
}

/*Represents an IO action that has finished, meaning we want to return the value
 * without any further steps*/
data class Return<A>(val a: A) : IO<A>()
/*We want to execute some effect to produce a result*/
data class Suspend<A>(val resume: InterpreterContext.() -> A) : IO<A>()
/*Lets us extend or continue a computation by using the result of the first computation
 * to produce the 2nd one*/
data class FlatMap<A, B>(val sub: IO<A>, val f: (A) -> IO<B>) : IO<B>()

object IOs {
  fun stdout(msg: String) = Suspend { printMessage(msg) }

  fun stdin(): IO<String> = Suspend { readLineFromInput().orEmpty() }

  fun gameScreen(programState: ProgramState) = Screen.io(programState)

  fun gameInput(programState: ProgramState): IO<ProgramState> = Input.io(programState)
}

interface InterpreterContext {
  fun printMessage(message: Any?)

  fun readLineFromInput(): String?
}

object DefaultInterpreterContext : InterpreterContext {
  override fun printMessage(message: Any?) = print(message)

  override fun readLineFromInput(): String? = readLine()
}

object Interpreter {
  // Page 306 in book

  tailrec fun <A> run(
    io: IO<A>,
    interpreterContext: InterpreterContext = DefaultInterpreterContext
  ): A =
    when (io) {
      is Return -> io.a
      is Suspend -> io.resume(interpreterContext)
      is FlatMap<*, *> -> {
        val x = io.sub as IO<A>
        val f = io.f as (A) -> IO<A>
        when (x) {
          is Return -> run(f(x.a), interpreterContext)
          is Suspend -> run(f(x.resume(interpreterContext)), interpreterContext)
          is FlatMap<*, *> -> {
            val g = x.f as (A) -> IO<A>
            val y = x.sub as IO<A>
            run(y.flatMap { a: A -> g(a).flatMap(f) }, interpreterContext)
          }
        }
      }
    }
}
