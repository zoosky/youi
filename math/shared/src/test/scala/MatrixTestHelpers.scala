import io.youi.math.{ImmutableMatrix3, Matrix3, MutableMatrix3}
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic._
import org.scalactic.TripleEquals._

trait MatrixTestHelpers {

  implicit val doubleEqualityTolerance: Equality[Double] =
    TolerantNumerics.tolerantDoubleEquality(0.0001)

  implicit val matrixEquality: Equality[Matrix3] = (m1: Matrix3, b: Any) =>
    b match {
      case m2: Matrix3 =>
        (m1.m00 === m2.m00) &&
          (m1.m01 === m2.m01) &&
          (m1.m02 === m2.m02) &&
          (m1.m10 === m2.m10) &&
          (m1.m11 === m2.m11) &&
          (m1.m12 === m2.m12) &&
          (m1.m20 === m2.m20) &&
          (m1.m21 === m2.m21) &&
          (m1.m22 === m2.m22)
      case _ => false
  }

  implicit val mutableGen: Gen[MutableMatrix3] = for {
    d <- Gen.listOfN(9, Gen.chooseNum[Double](-1e3, 1e3))
  } yield MutableMatrix3(d(0), d(1), d(2), d(3), d(4), d(5), d(6), d(7), d(8))

  implicit val mutableArbitrary = Arbitrary(mutableGen)

  implicit val immutableGen: Gen[ImmutableMatrix3] = for {
    d <- Gen.listOfN(9, Gen.chooseNum[Double](-1e3, 1e3))
  } yield ImmutableMatrix3(d(0), d(1), d(2), d(3), d(4), d(5), d(6), d(7), d(8))

  implicit val immutableArbitrary = Arbitrary(immutableGen)

  implicit val matrixGen: Gen[Matrix3] = Gen.oneOf(mutableGen, immutableGen)

  implicit val matrixArbitrary: Arbitrary[Matrix3] = Arbitrary(matrixGen)
}