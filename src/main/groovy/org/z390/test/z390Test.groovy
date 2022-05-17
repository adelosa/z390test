package org.z390.test

import org.junit.jupiter.api.AfterEach

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

    z390Test() {
        /**
         * class constructor
         */
        if (System.getenv('Z390_PROJECT_ROOT')) {
            this.project_root = System.getenv('Z390_PROJECT_ROOT')
        }
    }

    @AfterEach
    void cleanup() {
        if (this.tempDir)
            this.tempDir.deleteDir()
    }

    def createTempFile(String fileName, String fileContents) {
        /**
         * Utility method for creating temp option files used in tests
         */
        if (!tempDir) {
            this.tempDir = File.createTempDir()
            this.tempDir.deleteOnExit()
        }
        var fullFileName = pathJoin(tempDir.absolutePath, fileName)
        println("Creating temp source: ${fullFileName}")
        // to allow files to reference themselves, include {{fullFileName}} in contents
        fileContents = fileContents.replaceAll(/\{\{fullFileName}}/, fullFileName)
        println(fileContents)
        new File(fullFileName).with {
            createNewFile()
            write(fileContents)
        }
        return fullFileName
    }

    def basePath(String... pathItems) {
        var fullPathItems = [this.project_root, *pathItems]
        return fullPathItems.join(File.separator)
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
            var filename = basePath(asmFileExcludingExtension + '.' + ext)
            var deleteFile = new File(filename)
            if (deleteFile.exists()) {
                deleteFile.delete()
            }
        }
    }

    def callMz390(String asmFileExcludingExtension, String... args) {
        println("Executing mz390: ${asmFileExcludingExtension}")
        var cmd = ["java", "-classpath", basePath('jar', 'z390.jar'),
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

    def callLz390(String asmFileExcludingExtension, String... args) {
        println("Executing lz390: ${asmFileExcludingExtension}")
        var cmd = ["java", "-classpath", basePath('jar', 'z390.jar'),
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

    def callEz390(String asmFileExcludingExtension, String... args) {
        println("Executing ez390: ${asmFileExcludingExtension}")
        var cmd = ["java", "-classpath", basePath('jar', 'z390.jar'),
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
            this.tempDir = File.createTempDir()
            this.tempDir.deleteOnExit()
            asmFileExcludingExtension = pathJoin(tempDir.absolutePath, asmFilename)
            println("Creating temp source: ${asmFileExcludingExtension}")
            new File(pathJoin(tempDir.absolutePath, asmFilename + ".MLC")).with {
                createNewFile()
                write(asmSource)
            }
        } else {
            asmFileExcludingExtension = new File(pathJoin(asmFilename))
        }
        return asmFileExcludingExtension
    }

    def mz390(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
        this.reset(asmFileExcludingExtension)
        var rc = this.callMz390(asmFileExcludingExtension, args)
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

    def asm(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
        this.reset(asmFileExcludingExtension)
        var rc = this.callMz390(asmFileExcludingExtension, args)
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

    def lz390(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
//        this.reset(asmFileExcludingExtension)
        int rc = this.callLz390(asmFileExcludingExtension, args)
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

    def asml(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
        this.reset(asmFileExcludingExtension)
        var rc = this.callMz390(asmFileExcludingExtension, args)
        if (rc == 0) {
            rc = this.callLz390(asmFileExcludingExtension, args)
        }
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

    def ez390(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
//        this.reset(asmFileExcludingExtension)
        int rc = this.callEz390(asmFileExcludingExtension, args)
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

    def asmlg(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension = this.getAsmFileExcludingExtension(kwargs, asmFilename)
        this.reset(asmFileExcludingExtension)
        var rc = this.callMz390(asmFileExcludingExtension, args)
        if (rc == 0) {
            rc = this.callLz390(asmFileExcludingExtension, args)
            if (rc == 0) {
                rc = this.callEz390(asmFileExcludingExtension, args)
            }
        }
        this.getOutput(asmFileExcludingExtension)
        if (kwargs.get('asm_source') && this.tempDir)
            this.tempDir.deleteDir()
        return rc
    }

}
