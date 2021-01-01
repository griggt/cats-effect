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

import cats.{Monoid, Semigroup}
import cats.data.{EitherT, IorT, Kleisli, OptionT, ReaderWriterStateT, StateT, WriterT}
import cats.syntax.all._

trait SyncTG[F[_]]

object SyncTG {
  implicit def syncForOptionT[F[_]](implicit F0: SyncTG[F]): SyncTG[OptionT[F, *]] = ???
  implicit def syncForEitherT[F[_], E](implicit F0: SyncTG[F]): SyncTG[EitherT[F, E, *]] = ???
  implicit def syncForStateT[F[_], S](implicit F0: SyncTG[F]): SyncTG[StateT[F, S, *]] = ???
  implicit def syncForWriterT[F[_], L](implicit F0: SyncTG[F], L0: Monoid[L]): SyncTG[WriterT[F, L, *]] = ???
  implicit def syncForIorT[F[_], L](implicit F0: SyncTG[F], L0: Semigroup[L]): SyncTG[IorT[F, L, *]] = ???
  implicit def syncForKleisli[F[_], R](implicit F0: SyncTG[F]): SyncTG[Kleisli[F, R, *]] = ???
  implicit def syncForReaderWriterStateT[F[_], R, L, S](implicit F0: SyncTG[F], L0: Monoid[L]): SyncTG[ReaderWriterStateT[F, R, L, S, *]] = ???
}

trait AsyncTG[F[_]] extends SyncTG[F]

object AsyncTG {
  implicit def asyncForOptionT[F[_]](implicit F0: AsyncTG[F]): AsyncTG[OptionT[F, *]] = ???
  implicit def asyncForEitherT[F[_], E](implicit F0: AsyncTG[F]): AsyncTG[EitherT[F, E, *]] = ???
  implicit def asyncForIorT[F[_], L](implicit F0: AsyncTG[F], L0: Semigroup[L]): AsyncTG[IorT[F, L, *]] = ???
  implicit def asyncForWriterT[F[_], L](implicit F0: AsyncTG[F], L0: Monoid[L]): AsyncTG[WriterT[F, L, *]] = ???
  implicit def asyncForKleisli[F[_], R](implicit F0: AsyncTG[F]): AsyncTG[Kleisli[F, R, *]] = ???
}

abstract class RefTG[F[_], A]

object RefTG {
  trait Make[F[_]]
  object Make extends MakeInstances

  private[kernel] trait MakeInstances extends MakeLowPriorityInstances {
    implicit def concurrentInstance[F[_]](implicit F: GenConcurrent[F, _]): Make[F] = ???
  }

  private[kernel] trait MakeLowPriorityInstances {
    implicit def syncInstance[F[_]](implicit F: SyncTG[F]): Make[F] = ???
  }

  def of[F[_], A](a: A)(implicit mk: Make[F]): F[RefTG[F, A]] = ???
}

/***/

class Resource[F[_], A] {

  implicit def catsEffectAsyncForResource[F[_]](implicit F0: AsyncTG[F]): AsyncTG[Resource[F, *]] = ???

  def parZip(implicit F: Concurrent[F]) = {
    RefTG.of /*[F, (F[Unit], F[Unit])]*/ (().pure[F] -> ().pure[F])

    ()
  }
}
