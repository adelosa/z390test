package org.z390.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class z390Test {

    var project_root = pathJoin('..', 'z390')
    String stdout = ""
    String stderr = ""
    var env = [:]
    var fileData = [:]
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
    @BeforeEach
    void setUp() {
        this.stdout = ""
        this.stderr = ""
        this.fileData = [:]
    }
    @AfterEach
    void cleanUp() {
        if (this.tempDir)
            this.tempDir.deleteDir()
    }

    def createTempFile(String fileName, String fileContents, boolean returnExt=true) {
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
        new File(fullFileName).with {
            createNewFile()
            write(fileContents)
        }
        String ext = fullFileName.substring(fullFileName.lastIndexOf("."))
       if (!returnExt || ext.toUpperCase() in ('.MLC'))
            fullFileName = filenameWithoutExtension(fullFileName)
        return fullFileName
    }

    String basePath(String... pathItems) {
        var fullPathItems = [this.project_root, *pathItems]
        return fullPathItems.join(File.separator)
    }

    static String filenameWithoutExtension(String filename) {
        String result = filename[0..<filename.lastIndexOf('.')]
        println(result)
        return result
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

    def clean(String asmFileExcludingExtension) {
        /**
         * clear files for module
         */
        for (ext in ["PRN", "ERR", "OBJ", "390", "OBJ", "LOG"]) {
            var filename = basePath(asmFileExcludingExtension + '.' + ext)
            var deleteFile = new File(filename)
            if (deleteFile.exists()) {
                deleteFile.delete()
            }
        }
    }

    def callZ390(String asmFileExcludingExtension, String command, String... args) {
        println("Executing ${command}: ${asmFileExcludingExtension}")
        var cmd = ["java", "-classpath", basePath('jar', 'z390.jar'),
                   '-Xrs', '-Xms150000K', '-Xmx150000K', command, asmFileExcludingExtension, *args].join(" ")
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
            this.loadFile(filename, ext)
        }
    }

    def loadFile(String filename, String label) {
        var outFile = new File(filename)
        if (outFile.exists()) {
            println("reading ${filename}")
            this.fileData[label] = outFile.text
        }
    }

    static printFile(data, label) {
        String linefeed = /\n\r|\n/
        String[] lines = data.toString().split(linefeed)
        println(("*" * 20) + " ${label} (${lines.length} lines) " + ("*" * 20))
        lines.eachWithIndex{ String line, int lineNum ->
            println("${String.format('%05d',lineNum)}  ${line}")
        }
    }

    def printOutput() {
        printFile(this.stdout, 'stdout')
        printFile(this.stderr, 'stderr')
        this.fileData.each { fileExt, data ->
            printFile(data, fileExt)
        }
    }

    def mz390 = this.&asm

    def asm(Map kwargs=[:], String asmFilename, String... args) {
        /**
         * Assemble
         */
        this.clean(asmFilename)
        int rc = this.callZ390(asmFilename, 'mz390', args)
        this.getOutput(asmFilename)
        return rc
    }

    def lz390(Map kwargs=[:], String asmFilename, String... args) {
        /**
         * lz390
         * !! NOTE !! Does not clean intermediate files
         */
        int rc = this.callZ390(asmFilename, 'lz390', args)
        this.getOutput(asmFilename)
        return rc
    }

    def asml(Map kwargs=[:], String asmFilename, String... args) {
        /**
         * Assemble and link
         */
        this.clean(asmFilename)
        int rc = this.callZ390(asmFilename, 'mz390', args)
        if (rc == 0) {
            rc = this.callZ390(asmFilename, 'lz390', args)
        }
        this.getOutput(asmFilename)
        return rc
    }

    def ez390(Map kwargs=[:], String asmFilename, String... args) {
        /**
         * ez390
         * !! NOTE !! Does not clean intermediate files
         */
        int rc = this.callZ390(asmFilename, 'ez390', args)
        this.getOutput(asmFilename)
        return rc
    }

    def asmlg(Map kwargs=[:], String asmFilename, String... args) {
        /**
         * Assemble, link and go
         */
        this.clean(asmFilename)
        int rc = this.callZ390(asmFilename, 'mz390', args)
        if (rc == 0) {
            rc = this.callZ390(asmFilename, 'lz390', args)
            if (rc == 0) {
                rc = this.callZ390(asmFilename, 'ez390', args)
            }
        }
        this.getOutput(asmFilename)
        return rc
    }
}
