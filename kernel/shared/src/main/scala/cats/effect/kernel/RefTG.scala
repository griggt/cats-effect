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

  implicit def catsInvariantForRef[F[_]: Functor]: Invariant[RefTG[F, *]] = ???
}
