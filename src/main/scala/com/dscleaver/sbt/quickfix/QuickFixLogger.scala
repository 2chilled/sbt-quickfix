package com.dscleaver.sbt.quickfix

import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.message._
import sbt._
import sbt.internal.util._

object QuickFixLogger {
  //[warn] /home/chief/work/srg/aoe/server/app/ch/srf/aoe/imports/Interpreter.scala:609: [wartremover:Nothing] Inferred type containing Nothing
  def append0(output: File, prefix: String, message: String): Unit =
    IO.append(output, "[%s] %s\n".format(prefix, message))

  def append0(output: File, prefix: String, file: File, line: Int, message: String): Unit =
    append0(output, prefix, "%s:%d: %s".format(file, line, message))
}

class QuickFixLogger(val output: File, vimExec: String, enableServer: Boolean)
  extends AbstractAppender("QuickFixLogger", null, null) {
  import QuickFixLogger._
  import VimInteraction._

  override def append(e: LogEvent): Unit = {

    val realMsg =
      (e.getMessage match {
        case o: ObjectMessage => o.getParameter match {
          case x: ObjectEvent[_] => x.message.toString
          case x: StringEvent => x.message.toString
          case _ => ""
        }
        case _ => ""
      }).replaceFirst("\\[Error\\]", "").replaceFirst("\\[Warning\\]", "").trim

    println(s"XXXXXXXXXXXXXXXXXXX $realMsg")

    e.getLevel match {
      case Level.INFO => handleInfoMessage(realMsg)
      case Level.ERROR => handleErrorMessage(realMsg)
      case Level.WARN =>
        handleWarnMessage(realMsg)
      case _ =>
        handleDebugMessage(realMsg)
    }
  }

  def handleInfoMessage(message: String) = {
    println(s"DEBUUUUUUUG info: $message")
    //if (message.toLowerCase.contains("initial source changes:")) {
    if (message.toLowerCase.contains("compiling")) {
      IO.delete(output)
      IO.touch(List(output))

      if (enableServer) {
        println("CALLING cgetfile")
        call(vimExec, "cgetfile %s".format(output.toString))
      }
    }

  }

  def handleDebugMessage(message: String) = ()

  val regex = "([%s])(:%i)".r
  def handleErrorMessage(message: String) = {
    if (enableServer && message.toLowerCase.contains("compilation failed")) {
      println("CALLING cfile")
      call(vimExec, "cfile %s".format(output.toString))
    } else {
      append0(output, "error", message)
    }
  }

  def handleWarnMessage(message: String) = {
    val regex = """.+\.scala:[0-9]+:""".r
    //if(regex.findFirstIn(message).isDefined)
      append0(output, "warn", message)
  }

  def control(event: ControlEvent.Value, message: => String): Unit = {
    println(s"Control event $event, message = $message")
  }

  def logAll(events: Seq[LogEvent]): Unit = ()

  def success(message: => String): Unit = ()

  def trace(t: => Throwable): Unit = ()

}
