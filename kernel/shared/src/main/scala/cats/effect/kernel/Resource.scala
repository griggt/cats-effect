/*
 * Copyright 2020 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats.effect.kernel

import cats._
import cats.data.Kleisli
import cats.syntax.all._
import cats.effect.kernel.instances.spawn
import cats.effect.kernel.implicits._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class Resource[F[_], A] {
  import Resource._

  def parZip(implicit F: Concurrent[F]) = {
    Ref.of/*[F, (F[Unit], F[Unit])]*/(().pure[F] -> ().pure[F])

    ()
  }
}

object Resource {
  implicit def catsEffectAsyncForResource[F[_]](implicit F0: Async[F]): Async[Resource[F, *]] = ???
}
