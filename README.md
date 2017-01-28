# Smartypants

Simple smart constructor generation for Scala.

Copyright 2015 Dave Gurnell. Licensed [Apache 2][license].

[![Build Status](https://travis-ci.org/davegurnell/smartypants.svg?branch=develop)](https://travis-ci.org/davegurnell/smartypants)
[![Coverage status](https://img.shields.io/codecov/c/github/davegurnell/smartypants/develop.svg)](https://codecov.io/github/davegurnell/smartypants)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.davegurnell/smartypants_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.davegurnell/smartypants_2.11)

## Getting Started

Add the following to your `build.sbt`:

~~~ scala
libraryDependencies += "com.davegurnell" %% "smartypants" % "<<VERSION>>"
~~~

## Synopsis

Smartypants provides a `@smart` macro
to quickly define "smart constructors" for algebraic data types:

```scala
import smartypants._

sealed abstract class User extends Product with Serializable {
  def cookie: String
}

object User {
  @smart case class Anonymous(cookie: String) extends User
  @smart case class LoggedIn(email: String, cookie: String) extends User
}

val a = User.Anonymous("aCookie")
// a: User.Anonymous = Anonymous(aCookie)

val b = User.anonymous("aCookie")
// b: User = Anonymous(aCookie)
```

In this example, the `@smart` annotations
define two constructor methods
called `anonymous` and `loggedIn`.
Each method takes the same number of parameters
as its respective class
and returns an instance of the class typed as a `User`.

The main use case for smart constructors is
as a workaround for unhelpful type inference.
For example:

```scala
implicit val userOrdering: Ordering[User] =
  Ordering.by(_.cookie)

val users = List(User.Anonymous("cookie2"), User.Anonymous("cookie1"))
// users: List[User.Anonymous] = List(Anonymous(cookie2), Anonymous(cookie1))

val sorted = users.sorted
// <console>:17: error: No implicit Ordering defined for User.Anonymous.
//        users.sorted
//              ^
```

The problem here is that `users` is of type `List[User.Anonymous]`,
not `List[User]` as our ordering requires.
We can fix this issue by inserting type annotations:

```scala
(users : List[User]).sorted
```

but our smart constructors let us bypass the problem altogether:

```scala
val users1 = List(User.anonymous("cookie2"), User.anonymous("cookie1"))
// users1: List[User] = List(Anonymous(cookie2), Anonymous(cookie1))

val sorted1 = users1.sorted
// sorted1: List[User] = List(Anonymous(cookie1), Anonymous(cookie2))
```

You can use `@smart` to annotate *any inner class or object*.

## Customisation

The `@smart` macro infers the name and type of the constructor
from the annotated class or object:

```scala
// name is foo, return type is Bar
@smart case object Foo extends Bar

// name is baz, return type is Bar
@smart case class Baz(value: Int) extends Bar
```

You can customise the generated name by providing a value parameter:

```scala
@smart("alternativeName") case object Foo extends Bar
```

and the return type by providing a type parameter:

```scala
@smart[AnyRef] case class Baz(value: Int) extends Bar
```

You can even customise both at the same time:

```scala
@smart[AnyRef]("blah") case class Baz(value: Int) extends Bar
```

See the [tests] for more examples.

[license]: http://www.apache.org/licenses/LICENSE-2.0
[tests]: https://github.com/davegurnell/unindent/blob/master/src/test/scala/unindent/UnindentSpec.scala
