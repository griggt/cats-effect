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

import cats.{Applicative, Monoid, Semigroup, Defer}
import cats.data.{EitherT, Ior, IorT, Kleisli, OptionT, WriterT, ReaderWriterStateT, StateT}
import cats.syntax.all._

trait SyncTG[F[_]] extends MonadCancel[F, Throwable] with Clock[F] with Defer[F] {

  private[this] val Delay = SyncTG.Type.Delay
  private[this] val Blocking = SyncTG.Type.Blocking
  private[this] val InterruptibleOnce = SyncTG.Type.InterruptibleOnce
  private[this] val InterruptibleMany = SyncTG.Type.InterruptibleMany

  override def applicative: Applicative[F] = this

  def delay[A](thunk: => A): F[A] =
    suspend(Delay)(thunk)

  def defer[A](thunk: => F[A]): F[A] =
    flatMap(delay(thunk))(x => x)

  def blocking[A](thunk: => A): F[A] =
    suspend(Blocking)(thunk)

  def interruptible[A](many: Boolean)(thunk: => A): F[A] =
    suspend(if (many) InterruptibleOnce else InterruptibleMany)(thunk)

  def suspend[A](hint: SyncTG.Type)(thunk: => A): F[A]
}

object SyncTG {
  implicit def syncForOptionT[F[_]](implicit F0: SyncTG[F]): SyncTG[OptionT[F, *]] =
    new OptionTSync[F] {
      def rootCancelScope = F0.rootCancelScope
      implicit def F: SyncTG[F] = F0
    }

  implicit def syncForEitherT[F[_], E](implicit F0: SyncTG[F]): SyncTG[EitherT[F, E, *]] =
    new EitherTSync[F, E] {
      def rootCancelScope = F0.rootCancelScope
      implicit def F: SyncTG[F] = F0
    }

  implicit def syncForStateT[F[_], S](implicit F0: SyncTG[F]): SyncTG[StateT[F, S, *]] =
    new StateTSync[F, S] {
      def rootCancelScope = F0.rootCancelScope
      implicit def F: SyncTG[F] = F0
    }

  implicit def syncForWriterT[F[_], L](implicit F0: SyncTG[F], L0: Monoid[L]): SyncTG[WriterT[F, L, *]] =
    new WriterTSync[F, L] {
      def rootCancelScope = F0.rootCancelScope
      implicit def F: SyncTG[F] = F0
      implicit def L: Monoid[L] = L0
    }

  implicit def syncForIorT[F[_], L](implicit F0: SyncTG[F], L0: Semigroup[L]): SyncTG[IorT[F, L, *]] =
    new IorTSync[F, L] {
      def rootCancelScope = F0.rootCancelScope
      implicit def F: SyncTG[F] = F0
      implicit def L: Semigroup[L] = L0
    }

  implicit def syncForKleisli[F[_], R](implicit F0: SyncTG[F]): SyncTG[Kleisli[F, R, *]] =
    new KleisliSync[F, R] {
      def rootCancelScope = F0.rootCancelScope
      implicit def F: SyncTG[F] = F0
    }

  implicit def syncForReaderWriterStateT[F[_], R, L, S](implicit F0: SyncTG[F], L0: Monoid[L]): SyncTG[ReaderWriterStateT[F, R, L, S, *]] =
    new ReaderWriterStateTSync[F, R, L, S] {
      def rootCancelScope = F0.rootCancelScope
      implicit override def F: SyncTG[F] = F0
      implicit override def L: Monoid[L] = L0
    }

  private[effect] trait OptionTSync[F[_]]
      extends SyncTG[OptionT[F, *]]
      with MonadCancel.OptionTMonadCancel[F, Throwable]
      with Clock.OptionTClock[F] {

    implicit protected def F: SyncTG[F]
    protected def C = F

    def suspend[A](hint: Type)(thunk: => A): OptionT[F, A] =
      OptionT.liftF(F.suspend(hint)(thunk))
  }

  private[effect] trait EitherTSync[F[_], E]
      extends SyncTG[EitherT[F, E, *]]
      with MonadCancel.EitherTMonadCancel[F, E, Throwable]
      with Clock.EitherTClock[F, E] {
    implicit protected def F: SyncTG[F]
    protected def C = F

    def suspend[A](hint: Type)(thunk: => A): EitherT[F, E, A] =
      EitherT.liftF(F.suspend(hint)(thunk))
  }

  private[effect] trait StateTSync[F[_], S]
      extends SyncTG[StateT[F, S, *]]
      with MonadCancel.StateTMonadCancel[F, S, Throwable]
      with Clock.StateTClock[F, S] {
    implicit protected def F: SyncTG[F]
    protected def C = F

    def suspend[A](hint: Type)(thunk: => A): StateT[F, S, A] =
      StateT.liftF(F.suspend(hint)(thunk))
  }

  private[effect] trait WriterTSync[F[_], S]
      extends SyncTG[WriterT[F, S, *]]
      with MonadCancel.WriterTMonadCancel[F, S, Throwable]
      with Clock.WriterTClock[F, S] {
    implicit protected def F: SyncTG[F]
    protected def C = F

    def suspend[A](hint: Type)(thunk: => A): WriterT[F, S, A] =
      WriterT.liftF(F.suspend(hint)(thunk))
  }

  private[effect] trait IorTSync[F[_], L]
      extends SyncTG[IorT[F, L, *]]
      with MonadCancel.IorTMonadCancel[F, L, Throwable]
      with Clock.IorTClock[F, L] {
    implicit protected def F: SyncTG[F]
    protected def C = F

    def suspend[A](hint: Type)(thunk: => A): IorT[F, L, A] =
      IorT.liftF(F.suspend(hint)(thunk))
  }

  private[effect] trait KleisliSync[F[_], R]
      extends SyncTG[Kleisli[F, R, *]]
      with MonadCancel.KleisliMonadCancel[F, R, Throwable]
      with Clock.KleisliClock[F, R] {
    implicit protected def F: SyncTG[F]
    protected def C = F

    def suspend[A](hint: Type)(thunk: => A): Kleisli[F, R, A] =
      Kleisli.liftF(F.suspend(hint)(thunk))
  }

  private[effect] trait ReaderWriterStateTSync[F[_], R, L, S]
      extends SyncTG[ReaderWriterStateT[F, R, L, S, *]]
      with MonadCancel.ReaderWriterStateTMonadCancel[F, R, L, S, Throwable]
      with Clock.ReaderWriterStateTClock[F, R, L, S] {
    implicit protected def F: SyncTG[F]
    protected def C = F

    def suspend[A](hint: Type)(thunk: => A): ReaderWriterStateT[F, R, L, S, A] =
      ReaderWriterStateT.liftF(F.suspend(hint)(thunk))
  }

  sealed trait Type extends Product with Serializable

  object Type {
    case object Delay extends Type
    case object Blocking extends Type
    case object InterruptibleOnce extends Type
    case object InterruptibleMany extends Type
  }
}

trait AsyncTG[F[_]] extends SyncTG[F]

object AsyncTG {
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
