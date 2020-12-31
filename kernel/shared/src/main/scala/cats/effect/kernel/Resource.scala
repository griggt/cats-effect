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

//import cats._
import cats.data.{EitherT, Ior, IorT, Kleisli, OptionT, WriterT}
import cats.{~>, Monoid, Semigroup}
import cats.syntax.all._
//import cats.effect.kernel.implicits._

import java.util.concurrent.CompletableFuture
import scala.concurrent.{ExecutionContext, Future}
import scala.annotation.tailrec

private[kernel] trait AsyncPlatformTG[F[_]] { this: AsyncTG[F] =>
  def fromCompletableFuture[A](fut: F[CompletableFuture[A]]): F[A] = ???
}


trait AsyncTG[F[_]] extends AsyncPlatformTG[F] with Sync[F] with Temporal[F] {
  def async[A](k: (Either[Throwable, A] => Unit) => F[Option[F[Unit]]]): F[A] = ???
  def async_[A](k: (Either[Throwable, A] => Unit) => Unit): F[A] = ???
  def never[A]: F[A] = ???

  def evalOn[A](fa: F[A], ec: ExecutionContext): F[A]
  def executionContext: F[ExecutionContext]

  def fromFuture[A](fut: F[Future[A]]): F[A] = ???

  def cont[K, R](body: Cont[F, K, R]): F[R]
}

object AsyncTG {
  def apply[F[_]](implicit F: AsyncTG[F]): F.type = F
  def defaultCont[F[_], A](body: Cont[F, A, A])(implicit F: AsyncTG[F]): F[A] = ???

  implicit def asyncForOptionT[F[_]](implicit F0: AsyncTG[F]): AsyncTG[OptionT[F, *]] = ???
  implicit def asyncForEitherT[F[_], E](implicit F0: AsyncTG[F]): AsyncTG[EitherT[F, E, *]] = ???
  implicit def asyncForIorT[F[_], L](implicit F0: AsyncTG[F], L0: Semigroup[L]): AsyncTG[IorT[F, L, *]] = ???
  implicit def asyncForWriterT[F[_], L](implicit F0: AsyncTG[F], L0: Monoid[L]): AsyncTG[WriterT[F, L, *]] = ???
  implicit def asyncForKleisli[F[_], R](implicit F0: AsyncTG[F]): AsyncTG[Kleisli[F, R, *]] = ???
}

/***/

class Resource[F[_], A] {

  implicit def catsEffectAsyncForResource[F[_]](implicit F0: AsyncTG[F]): AsyncTG[Resource[F, *]] = ???

  def parZip(implicit F: Concurrent[F]) = {
    Ref.of /*[F, (F[Unit], F[Unit])]*/ (().pure[F] -> ().pure[F])

    ()
  }
}

