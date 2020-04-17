package com.softwaremill.refinednewtype

import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._
import io.estatico.newtype.Coercible
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

object CirceExample {

    object Models {
        type RecipientRules = NonEmpty
        type MessageBodyRules = NonEmpty

        type RecipientType = String Refined RecipientRules
        type MessageBodyType = String Refined MessageBodyRules

        @newtype case class Recipient(value: RecipientType)

        @newtype case class MessageBody(value: MessageBodyType)

        case class Message(recipient: Recipient, body: MessageBody)
    }

    trait CoercibleCodecs {

        implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
            Decoder[B].map(_.coerce[A])

        implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
            Encoder[B].contramap(_.repr.asInstanceOf[B])

        implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] =
            KeyDecoder[B].map(_.coerce[A])

        implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
            KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])
    }

    object Codecs extends CoercibleCodecs {

        import io.circe.refined._
        import Models._

        implicit val messageEncoder: Encoder[Message] = deriveEncoder[Message]
        implicit val messageDecoder: Decoder[Message] = deriveDecoder[Message]

    }

    object ManuallyDerivedCodecs {
        import Models._

        implicit val messageEncoder: Encoder[Message] =
            Encoder.forProduct2[Message, String, String]("recipient", "body") { msg =>
                (msg.recipient.coerce, msg.body.coerce)
            }

        implicit val recipientDecoder: Decoder[Recipient] = Decoder.decodeString.emap(refineV[RecipientRules](_).map(_.coerce[Recipient]))
        implicit val messageBodyDecoder: Decoder[MessageBody] = Decoder.decodeString.emap(refineV[MessageBodyRules](_).map(_.coerce[MessageBody]))

        implicit val messageDecoder: Decoder[Message] = Decoder.forProduct2("recipient", "body")(Message.apply)
    }


    def main(args: Array[String]): Unit = {

        import Models._
        import Codecs._
//        import ManuallyDerivedCodecs._

        val recipient = Recipient("@marcin")
        val body = MessageBody("How are you?")

        val msg = Message(recipient, body)

        val encodedMsg = msg.asJson.noSpaces
        // {"recipient":"@marcin","body":"How are you?"}
        println(encodedMsg)

        println(decode[Message](encodedMsg))
        // Right(Message(marcin,How are you?))

        println(decode[Message]("""{ "recipient": "", "body": "How are you?" }"""))
        // Left(DecodingFailure(Predicate isEmpty() did not fail., List(DownField(recipient))))
    }

}
