package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class OutputContractItem private (
    name: String,
    format: String,
    useJudge: Boolean,
    order: Option[String]
)

object OutputContractItem:
  private val DefaultUseJudge = true

  def parse(
      name: String,
      format: String,
      useJudge: Option[Boolean],
      order: Option[String]
  ): Either[PieceDefinitionError, OutputContractItem] =
    for
      parsedName <- parseName(name)
      parsedFormat <- parseFormat(format)
      parsedOrder <- parseOrder(order)
    yield OutputContractItem(
      name = parsedName,
      format = parsedFormat,
      useJudge = useJudge.getOrElse(DefaultUseJudge),
      order = parsedOrder
    )

  def create(
      name: String,
      format: String,
      useJudge: Option[Boolean],
      order: Option[String]
  ): Either[PieceDefinitionError, OutputContractItem] =
    parse(
      name = name,
      format = format,
      useJudge = useJudge,
      order = order
    )

  private def parseName(value: String): Either[PieceDefinitionError, String] =
    if value.isEmpty then Left(PieceDefinitionError.EmptyOutputContractName)
    else Right(value)

  private def parseFormat(value: String): Either[PieceDefinitionError, String] =
    if value.isEmpty then Left(PieceDefinitionError.EmptyOutputContractFormat)
    else Right(value)

  private def parseOrder(value: Option[String]): Either[PieceDefinitionError, Option[String]] =
    value match
      case None => Right(None)
      case Some(order) =>
        if order.isEmpty then Left(PieceDefinitionError.EmptyOutputContractOrder)
        else Right(Some(order))
