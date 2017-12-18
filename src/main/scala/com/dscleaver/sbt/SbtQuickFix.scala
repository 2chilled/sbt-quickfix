package com.dscleaver.sbt

import sbt._
import sbt.IO._

import Keys._
import quickfix.{ QuickFixLogger, VimPlugin, QuickFixTestListener }

object SbtQuickFix extends AutoPlugin {

  object QuickFixKeys {
    val QuickFixDirectory = target in config("quickfix")
    val quickFixInstall = TaskKey[Unit]("install-vim-plugin")
    val vimEnableServer = SettingKey[Boolean]("vim-enable-server", "Enables communication with the Vim server - requires that Vim has been compiled with +clientserver")
    val vimExecutable = SettingKey[String]("vim-executable", "The path to the vim executable, or just 'vim' if it's in the PATH already")
    val vimPluginBaseDirectory = SettingKey[File]("vim-plugin-directory", "The path where vim plugins should be installed")
  }

  import QuickFixKeys._

  override def trigger = allRequirements

  override val projectSettings = Seq(
    QuickFixDirectory := target.value / "quickfix",
    vimPluginBaseDirectory in ThisBuild := file(System.getProperty("user.home")) / ".vim" / "bundle",
    vimEnableServer in ThisBuild := true,
    extraLoggers := {
      val currentFunction = extraLoggers.value
      (key: ScopedKey[_]) => {
        val loggers = currentFunction(key)
        val taskOption = key.scope.task.toOption
        val logger = {
          val x = new QuickFixLogger(QuickFixDirectory.value / "sbt.quickfix", vimExecutable.value, vimEnableServer.value)
          x.start
          x
        }
        if (taskOption.map(_.label.startsWith("compile")) == Some(true))
          logger +: loggers
        else
          loggers
    }
    },
    testListeners += QuickFixTestListener(QuickFixDirectory.value / "sbt.quickfix", (sources in Test).value, vimExecutable.value, vimEnableServer.value),
    quickFixInstall in ThisBuild := VimPlugin.install(vimPluginBaseDirectory.value, streams.value),
    vimExecutable in ThisBuild := (if (System.getProperty("os.name").startsWith("Win")) "gvim.bat" else "gvim")
  )
}
