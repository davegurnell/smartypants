package smartypants

import scala.reflect.macros.blackbox

@macrocompat.bundle
class Macros(val c: blackbox.Context) {
  import c.universe._

  def smartsMacro(annottees: c.Expr[Any]*): c.Expr[Any] =
    annottees.map(_.tree).toList match {
      case (target @ q"..$mods object $name extends {..$earlyDefns } with ..$supers { $self => ..$defns }") :: _ =>

        def nameFromTree(tree: Tree): Option[Name] =
          tree match {
            case Ident(n)               => Some(n)
            case Apply(Ident(n), _)     => Some(n)
            case TypeApply(Ident(n), _) => Some(n)
            case _                      => None
          }

        val supertypeName: Name =
          constructorTypeFromAnnotation
            .flatMap(nameFromTree)
            .getOrElse(name match { case TermName(n) => TypeName(n) })

        def hasTypeName(name: Name)(tree: Tree): Boolean =
          nameFromTree(tree).contains(supertypeName)

        val smartsDefns: List[Tree] =
          defns.flatMap {
            case defn @ q"..$_ object $_ extends { ..$_ } with ..$supers { $_ => ..$_ }" =>
              println("supertype check " + supers + " " + supertypeName + " " + supers.exists(hasTypeName(supertypeName)))
              if(supers.exists(hasTypeName(supertypeName))) smartMacroImpl(defn) else List(defn)
            case defn @ q"..$_ class $_[..$_] $_(...$_) extends { ..$_} with ..$supers { $_ => ..$_ }" =>
              println("supertype check " + supers + " " + supertypeName + " " + supers.exists(hasTypeName(supertypeName)))
              if(supers.exists(hasTypeName(supertypeName))) smartMacroImpl(defn) else List(defn)
            case defn =>
              println("no supertype check " + defn)
              List(defn)
          }

        c.Expr[Any](q"$mods object $name extends { ..$earlyDefns } with ..$supers { $self => ..$smartsDefns } ; ")

      case _ =>
        c.abort(c.enclosingPosition, "Whu?! Annotation found without a class or object definition!")
    }

  def smartMacro(annottees: c.Expr[Any]*): c.Expr[Any] =
    annottees.map(_.tree).toList
      .headOption
      .map(smartMacroImpl)
      .map(trees => c.Expr[Any](trees.reduceLeft((a, b) => q"$a; $b")))
      .getOrElse(c.abort(c.enclosingPosition, "The @smart annotation can only be used on an inner class or object definition"))

  def smartMacroImpl(tree: Tree): List[Tree] =
    tree match {
      case defn @ q"..$mods object $name extends {..$earlyDefns } with ..$supers { $self => ..$defns }" =>
        val ctorName = constructorName(name.toString)
        val ctorType = constructorType(supers)
        val ctorDefn = q"def $ctorName: $ctorType = $name"

        // println(s"OBJECT ${name} => ${ctorDefn}")

        List(defn, ctorDefn)

      case defn @ q"..$mods class $tpe[..$tparams] $ctorMods(...$paramss) extends { ..$earlyDefns} with ..$supers { $self => ..$defns }" =>
        val ctorName    = constructorName(tpe.toString)
        val ctorType    = constructorType(supers)
        val ctorTParams = tparams
        val ctorParamss = constructorParamss(paramss)
        val exprParamss = expressionParamss(ctorParamss)
        val ctorDefn    = q"def $ctorName[..$ctorTParams](...$ctorParamss): $ctorType = new $tpe(...$exprParamss)"

        // println(s"CLASS ${tpe} => ${ctorDefn}")

        List(defn, ctorDefn)

      case defn =>
        // println(s"UNKNOWN ${defn} => FAIL")

        List(defn)
    }

  private def constructorName(base: String): TermName = {
    def defaultName = TermName(base.head.toLower + base.tail)

    def capitalise(name: TermName): TermName = {
      val TermName(n) = name
      TermName(n.head.toUpper + n.tail)
    }

    c.macroApplication match {
      case q"new smart[..$_]($param).macroTransform(..$_)" =>
        param match {
          case Literal(Constant(name: String)) =>
            TermName(name)
          case _ =>
            defaultName
        }

      case q"new smarts[..$_]($param).macroTransform(..$_)" =>
        param match {
          case Literal(Constant(prefix: String)) =>
            TermName(prefix + capitalise(defaultName))
          case _ =>
            defaultName
        }

      case _ =>
        defaultName
    }
  }

  private def constructorTypeFromAnnotation: Option[Tree] =
    c.macroApplication match {
      case q"new $_[$tparam, ..$_](...$_).macroTransform(..$_)" =>
        Some(tparam)
      case _ =>
        None
    }

  private def constructorTypeFromSupertypes(supers: List[Tree]): Option[Tree] =
    supers.headOption map {
      case Apply(tpe, params) => tpe
      case tpe => tpe
    }

  private def constructorType(supers: List[Tree]): Tree =
    constructorTypeFromAnnotation
      .orElse(constructorTypeFromSupertypes(supers))
      .getOrElse(c.abort(c.enclosingPosition, "Could not determine supertype"))

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
