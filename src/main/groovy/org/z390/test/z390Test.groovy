package org.z390.test

class z390Test {
    var project_root = pathJoin('..', 'z390')
    var stdout = ""
    var stderr = ""
    var env = [:]
    var fileOutput = [:]
    var message = "z390Test - Test framework for z390 project"
    int cmdTimeMs = 100000
    File tempDir = null

    static void main(String[] args) {
        println new z390Test().message
    }
    def static pathJoin(String... pathItems) {
        return pathItems.join(File.separator)
    }

    def getEnvList() {
        def envList = []
        for (key in this.env) {
            envList.add("${key}=${this.env[key]}")
        }
        return envList
    }

    def reset(String asmFileExcludingExtension) {
        this.stdout = ""
        this.stderr = ""
        this.fileOutput = [:]

        for (ext in ["PRN", "ERR", "OBJ", "390", "OBJ", "LOG"]) {
            var filename = pathJoin(asmFileExcludingExtension + '.' + ext)
            var deleteFile = new File(filename)
            if (deleteFile.exists()) {
                deleteFile.delete()
            }
        }
    }

    def mz390(String asmFileExcludingExtension, String... args) {
        println("Executing mz390: ${asmFileExcludingExtension}")
        var cmd = ["java", "-classpath", pathJoin(this.project_root, 'jar', 'z390.jar'),
                   '-Xrs', '-Xms150000K', '-Xmx150000K', 'mz390', asmFileExcludingExtension, *args].join(" ")
        println(cmd)
        var proc = cmd.execute(this.getEnvList(), null)   // , workDir);
        var sout = new StringBuilder()
        var serr = new StringBuilder()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(this.cmdTimeMs)
        this.stdout += sout
        this.stderr += serr
        return proc.exitValue()
    }

    def lz390(String asmFileExcludingExtension, String... args) {
        println("Executing lz390: ${asmFileExcludingExtension}")
        var cmd = ["java", "-classpath", pathJoin(this.project_root, 'jar', 'z390.jar'),
                   '-Xrs', 'lz390', asmFileExcludingExtension, *args].join(" ")
        println(cmd)
        var proc = cmd.execute(this.getEnvList(), null)   // , workDir);
        var sout = new StringBuilder()
        var serr = new StringBuilder()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(this.cmdTimeMs)
        this.stdout += sout
        this.stderr += serr
        return proc.exitValue()
    }

    def ez390(String asmFileExcludingExtension, String... args) {
        println("Executing ez390: ${asmFileExcludingExtension}")
        var cmd = ["java", "-classpath", pathJoin(this.project_root, 'jar', 'z390.jar'),
                   '-Xrs', 'ez390', asmFileExcludingExtension, *args].join(" ")
        println(cmd)
        var proc = cmd.execute(this.getEnvList(), null)   // , workDir);
        var sout = new StringBuilder()
        var serr = new StringBuilder()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(this.cmdTimeMs)
        this.stdout += sout
        this.stderr += serr
        return proc.exitValue()
    }

    def getOutput(String asmFileExcludingExtension) {
        for (String ext in ["PRN", "ERR", "LOG", "LST"]) {
            var filename = pathJoin(asmFileExcludingExtension + '.' + ext)
            var outFile = new File(filename)
            if (outFile.exists()) {
                println("reading ${ext} output")
                this.fileOutput[ext] = outFile.text
            }
        }
    }

    def printOutput() {
        println(("*" * 20) + " stdout " + ("*" * 20))
        this.stdout.lines().eachWithIndex{ String line, int lineNum ->
            println("${String.format('%05d',lineNum)}  ${line}")
        }
        println(("*" * 20) + " stderr " + ("*" * 20))
        this.stderr.lines().eachWithIndex{ String line, int lineNum ->
            println("${String.format('%05d',lineNum)}  ${line}")
        }
        this.fileOutput.each { fileExt, data ->
            println(("*" * 20) + " ${fileExt} " + ("*" * 20))
            String data_string = data
            data_string.lines().eachWithIndex{ String line, int lineNum ->
                println("${String.format('%05d',lineNum)}  ${line}")
            }
        }
    }

    String getAsmFileExcludingExtension(Map kwargs=[:], String asmFilename, String... args) {
        String asmFileExcludingExtension
        String asmSource = kwargs['asm_source'] ?: null
        if (asmSource) {
            tempDir = File.createTempDir()
            this.tempDir.deleteOnExit()
            asmFileExcludingExtension = pathJoin(tempDir.absolutePath, asmFilename)
            println("Creating temp source: ${asmFileExcludingExtension}")
            new File(pathJoin(tempDir.absolutePath, asmFilename + ".MLC")).with {
                createNewFile()
                write(asmSource)
            }
        } else {
            asmFileExcludingExtension = new File(pathJoin(this.project_root, asmFilename))
        }
        return asmFileExcludingExtension
    }

    def asm(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
        this.reset(asmFileExcludingExtension)
        var rc = this.mz390(asmFileExcludingExtension, args)
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

    def asml(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
        this.reset(asmFileExcludingExtension)
        var rc = this.mz390(asmFileExcludingExtension, args)
        if (rc == 0) {
            rc = this.lz390(asmFileExcludingExtension, args)
        }
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

    def asmlg(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
        this.reset(asmFileExcludingExtension)
        var rc = this.mz390(asmFileExcludingExtension, args)
        if (rc == 0) {
            rc = this.lz390(asmFileExcludingExtension, args)
            if (rc == 0) {
                rc = this.ez390(asmFileExcludingExtension, args)
            }
        }
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

}
