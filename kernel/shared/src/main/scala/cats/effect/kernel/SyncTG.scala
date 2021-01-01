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

