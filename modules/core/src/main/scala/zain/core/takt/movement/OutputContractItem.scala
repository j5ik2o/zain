package zain.core.takt.movement

final case class OutputContractItem private (
    name: OutputContractName,
    format: OutputContractFormat,
    useJudge: Boolean,
    order: Option[OutputContractOrder]
)

object OutputContractItem:
  private val DefaultUseJudge = true

  def parse(
      name: OutputContractName,
      format: OutputContractFormat,
      useJudge: Option[Boolean],
      order: Option[OutputContractOrder]
  ): Either[zain.core.takt.piece.PieceDefinitionError, OutputContractItem] =
    Right(
      OutputContractItem(
        name = name,
        format = format,
        useJudge = useJudge.getOrElse(DefaultUseJudge),
        order = order
      )
    )

  def create(
      name: OutputContractName,
      format: OutputContractFormat,
      useJudge: Option[Boolean],
      order: Option[OutputContractOrder]
  ): Either[zain.core.takt.piece.PieceDefinitionError, OutputContractItem] =
    parse(
      name = name,
      format = format,
      useJudge = useJudge,
      order = order
    )
