package smartypants

import scala.reflect.macros.blackbox

@macrocompat.bundle
class Macros(val c: blackbox.Context) {
  import c.universe._

  def smartestMacro[A: WeakTypeTag]: c.Expr[Unit] = {
    val tag = weakTypeOf[A]

    println("output from 'smartest' macro...")

    val supertype = tag.typeSymbol.typeSignature
    println(" - supertype " + supertype + " " + supertype.getClass.getSimpleName)

    // This gets the subtypes nicely
    // but we can't introduce new identifiers with a blackbox macro,
    // so it's not useful for introducing smart constructors:
    val subtypes = tag.typeSymbol.asClass.knownDirectSubclasses.map(_.asType)
    println(" - subtypes " + subtypes + " " + subtypes.getClass.getSimpleName)

    c.Expr[Unit](q"()")
  }

  def smarterMacro(annottees: c.Expr[Any]*): c.Expr[Any] =
    annottees.map(_.tree).toList match {
      case cls :: obj :: _ =>
        val q"..$_ class $tpe[..$tparams] $ctorMods(...$paramss) extends { ..$_} with ..$_ { $_ => ..$_ }" = cls
        val q"..$_ object $name extends {..$_ } with ..$_ { $_ => ..$defns }" = obj

        println("output from 'smarter' macro...")

        // This type checking doesn't work,
        // presumably for the reasons outlined by Chris Birchall here:
        // http://stackoverflow.com/questions/39916994/how-to-debug-a-macro-annotation
        val clsTpe = c.typecheck(Ident(tpe), mode = c.TYPEmode, silent = true, withMacrosDisabled = true)
        println(" - clsTpe " + clsTpe + " " + clsTpe.getClass.getSimpleName)

        val clsSubtpes = clsTpe.symbol.asClass.knownDirectSubclasses.map(_.asType)
        println(" - clsSubtpes " + clsSubtpes + " " + clsSubtpes.getClass.getSimpleName)

        c.Expr[Any](q"$cls; $obj")

      case cls :: _ =>
        println("class only")
        println(cls)
        c.Expr[Any](q"$cls")

      case Nil =>
        c.abort(c.enclosingPosition, "Whu?! Annotation found without a class or object definition!")
    }

  def smartMacro(annottees: c.Expr[Any]*): c.Expr[Any] =
    annottees.map(_.tree).toList match {
      case (target @ q"..$mods object $name extends {..$earlyDefns } with ..$supers { $self => ..$defns }") :: _ =>
        val ctorName = constructorName(name.toString)
        val ctorType = constructorType(supers)
        val ctorDefn = q"def $ctorName: $ctorType = $name"

        // println(s"OBJECT ${name} => ${ctorDefn}")

        c.Expr[Any](q"$target; $ctorDefn")

      case (target @ q"..$mods class $tpe[..$tparams] $ctorMods(...$paramss) extends { ..$earlyDefns} with ..$supers { $self => ..$defns }") :: _ =>
        val ctorName    = constructorName(tpe.toString)
        val ctorType    = constructorType(supers)
        val ctorTParams = tparams
        val ctorParamss = constructorParamss(paramss)
        val exprParamss = expressionParamss(ctorParamss)
        val ctorDefn    = q"def $ctorName[..$ctorTParams](...$ctorParamss): $ctorType = new $tpe(...$exprParamss)"

        // println(s"CLASS ${tpe} => ${ctorDefn}")

        c.Expr[Any](q"$target; $ctorDefn")

      case target :: _ =>
        // println(s"UNKNOWN ${target} => FAIL")

        c.abort(c.enclosingPosition, "The @smart annotation can only be used on an inner class or object definition")
    }

  private def constructorName(base: String): TermName = {
    def defaultName = TermName(base.head.toLower + base.tail)

    c.macroApplication match {
      case q"new smart[..$_]($param).macroTransform(..$_)" =>
        param match {
          case Literal(Constant(name: String)) =>
            TermName(name)
          case _ =>
            defaultName
        }
      case _ =>
        defaultName
    }
  }

  private def constructorType(supers: List[Tree]): Tree =
    c.macroApplication match {
      case q"new smart[$tparam, ..$_](...$_).macroTransform(..$_)" =>
        tparam
      case _ =>
        supers.head match {
          case Apply(tpe, params) => tpe
          case tpe => tpe
        }
    }

  private def constructorParamss(paramss: List[List[Tree]]): List[List[Tree]] =
    paramss.map { params =>
      params.map { param =>
        val q"$paramMods val $paramName: $paramType = $paramDefault" = param
        q"val ${TermName(paramName.toString)}: $paramType = ${paramDefault.duplicate}"
      }
    }

  private def expressionParamss(paramss: List[List[Tree]]): List[List[Tree]] =
    paramss.map { params =>
      params.map { param =>
        val q"$paramMods val $paramName: $paramType = $paramDefault" = param
        q"$paramName"
      }
    }

}
