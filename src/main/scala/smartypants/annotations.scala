package smartypants

import scala.annotation.StaticAnnotation

class smart[A](name: String = "") extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Macros.smartMacro
}

class smarter[A] extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Macros.smarterMacro
}

object Smartypants {
  def smartest[A]: Unit = macro Macros.smartestMacro[A]
}