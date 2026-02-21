package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class OutputContractItems private (
    private val values: Vector[OutputContractItem]
):
  def isEmpty: Boolean =
    values.isEmpty

  def map[B](transform: OutputContractItem => B): Vector[B] =
    values.map(transform)

  def lift(index: Int): Option[OutputContractItem] =
    values.lift(index)

  def breachEncapsulationOfValues: Vector[OutputContractItem] =
    values

object OutputContractItems:
  val Empty: OutputContractItems = OutputContractItems(Vector.empty)

  def parse(values: Vector[OutputContractItem]): Either[PieceDefinitionError, OutputContractItems] =
    parseUniqueNames(values).map(_ => OutputContractItems(values))

  def create(values: Vector[OutputContractItem]): Either[PieceDefinitionError, OutputContractItems] =
    parse(values)

  private def parseUniqueNames(
      values: Vector[OutputContractItem]
  ): Either[PieceDefinitionError, Unit] =
    values
      .foldLeft[Either[PieceDefinitionError, Set[OutputContractName]]](Right(Set.empty)) { (acc, item) =>
        acc.flatMap: seen =>
          if seen.contains(item.name) then Left(PieceDefinitionError.DuplicateOutputContractItemName(item.name.value))
          else Right(seen + item.name)
      }
      .map(_ => ())
