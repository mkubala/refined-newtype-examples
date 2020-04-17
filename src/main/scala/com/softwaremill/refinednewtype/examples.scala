package com.softwaremill.refinednewtype

import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.auto._
import eu.timepit.refined.boolean._
import eu.timepit.refined.collection._
import eu.timepit.refined.string._
import io.estatico.newtype.macros.newtype

object Simple {

    case class Message(recipient: String, body: String)

    Message("@marcin", "Are you there?")
    Message("#scala", "Let's deserialize some JSON!")
    Message("How are you?", "@marcin") // compiles - not good :(

}

object Tagged {

    import com.softwaremill.tagging._

    sealed trait Recipient

    sealed trait MessageBody

    case class Message(recipient: String @@ Recipient, body: String @@ MessageBody)

    val recipient: String @@ Recipient = "@marcin".taggedWith[Recipient]
    val messageBody: String @@ MessageBody = "How are you?".taggedWith[MessageBody]

    Message(recipient, messageBody)
    // Message(messageBody, recipient) // won't compile
    Message("notPrecededWithHashOrAtSign".taggedWith[Recipient], "".taggedWith[MessageBody]) // compiles - not good :(
}

object ValueClasses {

    case class Recipient(value: String) extends AnyVal

    case class MessageBody(value: String) extends AnyVal

    case class Message(recipient: Recipient, body: MessageBody)

    val recipient: Recipient = Recipient("@marcin")
    val messageBody: MessageBody = MessageBody("How are you?")
    Message(recipient, messageBody)
    // Message(messageBody, recipient) // won't compile
    Message(Recipient("notPrecededWithHashOrAtSign"), MessageBody("")) // compiles - not good :(
}

object Refined {

    type Recipient = String Refined ((StartsWith["@"] Or StartsWith["#"]) And MinSize[2])
    type MessageBody = String Refined NonEmpty

    val recipient1: Recipient = "@valid"
    val recipient2: Recipient = "#valid"
    // val recipient3: Recipient = "invalid" // won't compile - success!
    // val recipient4: Recipient = "" // won't compile - success!
    // val recipient5: Recipient = "@" // won't compile - success!
    // val recipient6: Recipient = "#" // won't compile - success!

    case class Message(recipient: Recipient, body: MessageBody)

    val recipient: Recipient = "@marcin"
    val body: MessageBody = "How are you today?"

    Message(recipient, body)
    // Message(body, recipient) // won't compile - success!
    // Message("How are you?", "@marcin") // won't compile but only because "How are you?" does not meet Recipient's requirements.
    Message("@greg is very clever!", "@marcin") // compiles, because the message body meets refined type requirements for Recipient

}

object ParsingRawInputs {
    type RecipientRules = (StartsWith["@"] Or StartsWith["#"]) And MinSize[2]
    type MessageBodyRules = NonEmpty

    type Recipient = String Refined RecipientRules
    type MessageBody = String Refined MessageBodyRules

    case class Message(recipient: Recipient, body: MessageBody)

    def parseMessage(rawRecipient: String, rawBody: String): Either[String, Message] = {

        for {
            recipient <- refineV[RecipientRules](rawRecipient)
            body <- refineV[MessageBodyRules](rawBody)
        } yield Message(recipient, body)
    }

    def main(args: Array[String]): Unit = {
        println(parseMessage("@marcin", "How are you?"))
        // res: Right(Message(@marcin,How are you?))

        println(parseMessage("marcin", "How are you?"))
        // res: Left(Left predicate of (("marcin".startsWith("@") || "marcin".startsWith("#")) && !(6 < 2)) failed:
        // Both predicates of ("marcin".startsWith("@") || "marcin".startsWith("#")) failed. Left: Predicate failed:
        // "marcin".startsWith("@"). Right: Predicate failed: "marcin".startsWith("#").)

        println(parseMessage("How are you?", "@marcin"))
        // res: Left(Left predicate of (("How are you?".startsWith("@") || "How are you?".startsWith("#")) && !(12 < 2)) failed:
        // Both predicates of ("How are you?".startsWith("@") || "How are you?".startsWith("#")) failed. Left: Predicate failed:
        // "How are you?".startsWith("@"). Right: Predicate failed: "How are you?".startsWith("#").)
    }

}

object RefinedIsNotEnough {

    type Recipient = String Refined NonEmpty
    type MessageBody = String Refined NonEmpty
    // ^ both aliases points to the same effective type

    case class Message(recipient: Recipient, body: MessageBody)

    Message("@marcin", "How are you?")
    Message("How are you?", "@marcin") // compiles - not good :(

}

object RefinedAndValueClasses {
    type RecipientType = String Refined NonEmpty
    type MessageBodyType = String Refined NonEmpty

    // won't compile - value class may not wrap another user-defined value class
    // case class Recipient(value: RecipientR) extends AnyVal
    // case class MessageBody(value: MessageBodyR) extends AnyVal
}

object RefinedAndNewtype {

    type RecipientType = String Refined NonEmpty
    type MessageBodyType = String Refined NonEmpty

    @newtype case class Recipient(value: RecipientType)

    @newtype case class MessageBody(value: MessageBodyType)

    case class Message(recipient: Recipient, body: MessageBody)

    val recipient = Recipient("marcin")
    val body = MessageBody("How are you?")

    Message(recipient, body)
    // Message(body, recipient) // won't compile - success!
    // val invalidRecipient = Recipient("") // won't compile - success!
}
