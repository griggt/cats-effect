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

package cats
package effect
package kernel

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import cats.data.State
import cats.effect.kernel.RefTG.TransformedRefTG
import cats.instances.function._
import cats.instances.tuple._
import cats.syntax.bifunctor._
import cats.syntax.functor._

abstract class RefTG[F[_], A] {

  def get: F[A]
  def set(a: A): F[Unit]

  def getAndUpdate(f: A => A): F[A] = ???
  def getAndSet(a: A): F[A] = ???
  def updateAndGet(f: A => A): F[A] = ???

  def access: F[(A, A => F[Boolean])]
  def tryUpdate(f: A => A): F[Boolean]
  def tryModify[B](f: A => (A, B)): F[Option[B]]
  def update(f: A => A): F[Unit]
  def modify[B](f: A => (A, B)): F[B]
  def tryModifyState[B](state: State[A, B]): F[Option[B]]
  def modifyState[B](state: State[A, B]): F[B]

  def mapK[G[_]](f: F ~> G)(implicit F: Functor[F]): RefTG[G, A] = ???
}

object RefTG {

  @annotation.implicitNotFound(
    "Cannot find an instance for Ref.Make. Add implicit evidence of Concurrent[${F}, _] or Sync[${F}] to scope to automatically derive one.")
  trait Make[F[_]] {
    def refOf[A](a: A): F[RefTG[F, A]]
  }

  object Make extends MakeInstances

  private[kernel] trait MakeInstances extends MakeLowPriorityInstances {
    implicit def concurrentInstance[F[_]](implicit F: GenConcurrent[F, _]): Make[F] = ???
  }

  private[kernel] trait MakeLowPriorityInstances {
    implicit def syncInstance[F[_]](implicit F: SyncTG[F]): Make[F] = ???
  }

  def apply[F[_]](implicit mk: Make[F]): ApplyBuilders[F] = ???
  def of[F[_], A](a: A)(implicit mk: Make[F]): F[RefTG[F, A]] = ???
  def copyOf[F[_]: Make: FlatMap, A](source: RefTG[F, A]): F[RefTG[F, A]] = ???
  def ofEffect[F[_]: Make: FlatMap, A](fa: F[A]): F[RefTG[F, A]] = ???
  def unsafe[F[_], A](a: A)(implicit F: SyncTG[F]): RefTG[F, A] = ???
  def in[F[_], G[_], A](a: A)(implicit F: SyncTG[F], G: SyncTG[G]): F[RefTG[G, A]] = ???
  def lens[F[_], A, B <: AnyRef](ref: RefTG[F, A])(get: A => B, set: A => B => A)(implicit F: SyncTG[F]): RefTG[F, B] = ???

  final class ApplyBuilders[F[_]](val mk: Make[F]) extends AnyVal {
    def of[A](a: A): F[RefTG[F, A]] = ???
  }

  final private class SyncRefTG[F[_], A](ar: AtomicReference[A])(implicit F: SyncTG[F]) extends RefTG[F, A] {
    def get: F[A] = ???
    def set(a: A): F[Unit] = ???
    override def getAndSet(a: A): F[A] = ???
    override def getAndUpdate(f: A => A): F[A] = ???
    def access: F[(A, A => F[Boolean])] = ???
    def tryUpdate(f: A => A): F[Boolean] = ???
    def tryModify[B](f: A => (A, B)): F[Option[B]] = ???
    def update(f: A => A): F[Unit] = ???
    override def updateAndGet(f: A => A): F[A] = ???
    def modify[B](f: A => (A, B)): F[B] = ???
    def tryModifyState[B](state: State[A, B]): F[Option[B]] = ???
    def modifyState[B](state: State[A, B]): F[B] = ???
  }

  final private[kernel] class TransformedRefTG[F[_], G[_], A](
      underlying: RefTG[F, A],
      trans: F ~> G)(
      implicit F: Functor[F]
  ) extends RefTG[G, A] {
    override def get: G[A] = ???
    override def set(a: A): G[Unit] = ???
    override def getAndSet(a: A): G[A] = ???
    override def tryUpdate(f: A => A): G[Boolean] = ???
    override def tryModify[B](f: A => (A, B)): G[Option[B]] = ???
    override def update(f: A => A): G[Unit] = ???
    override def modify[B](f: A => (A, B)): G[B] = ???
    override def tryModifyState[B](state: State[A, B]): G[Option[B]] = ???
    override def modifyState[B](state: State[A, B]): G[B] = ???
    override def access: G[(A, A => G[Boolean])] = ???
  }

  final private[kernel] class LensRefTG[F[_], A, B <: AnyRef](underlying: RefTG[F, A])(
      lensGet: A => B,
      lensSet: A => B => A
  )(implicit F: SyncTG[F])
      extends RefTG[F, B] {
    override def get: F[B] = ???
    override def set(b: B): F[Unit] = ???
    override def getAndSet(b: B): F[B] = ???
    override def update(f: B => B): F[Unit] = ???
    override def modify[C](f: B => (B, C)): F[C] = ???
    override def tryUpdate(f: B => B): F[Boolean] = ???
    override def tryModify[C](f: B => (B, C)): F[Option[C]] = ???
    override def tryModifyState[C](state: State[B, C]): F[Option[C]] = ???
    override def modifyState[C](state: State[B, C]): F[C] = ???
    override val access: F[(B, B => F[Boolean])] = ???
  }

  implicit def catsInvariantForRef[F[_]: Functor]: Invariant[RefTG[F, *]] = ???
}
