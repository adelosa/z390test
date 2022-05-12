package org.z390.test

class z390Test {
    var project_root = pathJoin('..', 'z390')
    var stdout = ""
    var stderr = ""
    var env = [:]
    var fileOutput = [:]
    var message = "z390Test - Test framework for z390 project"

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
        proc.waitForOrKill(1000)
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
            data.lines().eachWithIndex{ String line, int lineNum ->
                println("${String.format('%05d',lineNum)}  ${line}")
            }
        }
    }

    def asm(Map kwargs=[:], String asmFilename, String... args) {

        String asmFileExcludingExtension
        String asmSource = kwargs['asm_source'] ?: null
        File tempDir
        if (asmSource) {
            tempDir = File.createTempDir()
            tempDir.deleteOnExit()
            asmFileExcludingExtension = pathJoin(tempDir.absolutePath, asmFilename)
            println("Creating temp source: ${asmFileExcludingExtension}")
            new File(pathJoin(tempDir.absolutePath, asmFilename + ".MLC")).with {
                createNewFile()
                write(asmSource)
            }
        } else {
            asmFileExcludingExtension = new File(pathJoin(this.project_root, asmFilename))
        }
        println(asmFileExcludingExtension)

        this.reset(asmFileExcludingExtension)
        var rc = this.mz390(asmFileExcludingExtension, args)
        this.getOutput(asmFileExcludingExtension)
        if (asmSource && tempDir)
            tempDir.deleteDir()
        return rc
    }
}

/*
"""
A framework for testing z390 programs using python
"""
import os
import os.path as path
import shutil
import subprocess
import tempfile
import unittest


class Z390Test(unittest.TestCase):
    """
    Support z390 execution test cases
    Assumes that java runtime available
    project root points to root of z390 source
    z390.jar file expected in jar folder
    """
    project_root = path.join('..', '..')     # project root
    stdout = ''
    stderr = ''
    env = {}
    file_output = {}

    def setUp(self) -> None:
        super(Z390Test, self).setUp()
        self.env = {}

    def get_full_filepath(self, asm_file):
        return path.join(self.project_root, asm_file)

    def reset(self, asm_file_path):
        self.stdout = ''
        self.stderr = ''
        self.file_output = {}

        for ext in ['PRN', 'ERR', 'OBJ', '390', 'OBJ', 'LOG']:
            filename = path.join(asm_file_path + '.' + ext)
            if path.exists(filename):
                os.remove(filename)

    def get_output(self, asm_file_path):
        # capture the output listings
        # asm_file_path = self.get_full_filepath(asm_file)
        for ext in ['PRN', 'ERR', 'LST', 'LOG']:
            if path.exists(asm_file_path + '.' + ext):
                print('reading .{} output'.format(ext))
                with open(asm_file_path + '.' + ext, 'r') as prn_file:
                    self.file_output[ext] = prn_file.read()

    def asm(self, asm_file, *args, asm_source=None):
        # create the source prog in a temp folder
        temp_dir = None
        if asm_source:
            temp_dir = tempfile.mkdtemp()
            asm_file_path = path.join(temp_dir, asm_file)
            print('Creating temp source: {}'.format(asm_file_path))
            with open(asm_file_path + ".MLC", 'w') as source:
                source.write(asm_source)
        else:
            asm_file_path = self.get_full_filepath(asm_file)

        self.reset(asm_file_path)
        rc = self.mz390(asm_file_path, *args)
        self.get_output(asm_file_path)
        if temp_dir:
            shutil.rmtree(temp_dir)
        return rc

    def asml(self, asm_file, *args, asm_source=None):
        # create the source prog in a temp folder
        temp_dir = None
        if asm_source:
            temp_dir = tempfile.mkdtemp()
            asm_file_path = path.join(temp_dir, asm_file)
            print('Creating temp source: {}'.format(asm_file_path))
            with open(asm_file_path + ".MLC", 'w') as source:
                source.write(asm_source)
        else:
            asm_file_path = self.get_full_filepath(asm_file)

        self.reset(asm_file_path)
        rc = self.mz390(asm_file_path, *args)
        if rc == 0:
            rc = self.lz390(asm_file_path, *args)
        self.get_output(asm_file_path)
        if temp_dir:
            shutil.rmtree(temp_dir)
        return rc

    def asmlg(self, asm_file, *args, asm_source=None):
        # create the source prog in a temp folder
        temp_dir = None
        if asm_source:
            temp_dir = tempfile.mkdtemp()
            asm_file_path = path.join(temp_dir, asm_file)
            print('Creating temp source: {}'.format(asm_file_path))
            with open(asm_file_path + ".MLC", 'w') as source:
                source.write(asm_source)
        else:
            asm_file_path = self.get_full_filepath(asm_file)

        self.reset(asm_file_path)
        rc = self.mz390(asm_file_path, *args)
        if rc == 0:
            rc = self.lz390(asm_file_path, *args)
            if rc == 0:
                rc = self.ez390(asm_file_path, *args)
        self.get_output(asm_file_path)
        return rc

    def mz390(self, asm_file_path, *args):
        print('Executing mz390: {}'.format(asm_file_path))

        # asm_file_path = self.get_full_filepath(asm_file)
        full_env = {**os.environ, **self.env}
        result = subprocess.run(
            ["java", "-classpath", path.join(self.project_root, 'jar', 'z390.jar'),
             '-Xrs', '-Xms150000K', '-Xmx150000K', 'mz390', asm_file_path, *args],
            capture_output=True,
            env=full_env
        )

        # capture the output to std
        self.stdout += result.stdout.decode()
        self.stderr += result.stderr.decode()

        return result.returncode

    def lz390(self, asm_file_path, *args):
        print('Executing lz390: {}'.format(asm_file_path))

        # asm_file_path = self.get_full_filepath(asm_file)
        env = {**os.environ, **self.env}
        result = subprocess.run(
            ["java", "-classpath", path.join(self.project_root, 'jar', 'z390.jar'),
             '-Xrs', 'lz390', asm_file_path, *args],
            capture_output=True,
            env=env
        )

        # capture the output to std
        self.stdout += result.stdout.decode()
        self.stderr += result.stderr.decode()

        return result.returncode

    def ez390(self, asm_file_path, *args):
        print('Executing ez390: {}'.format(asm_file_path))

        full_env = {**os.environ, **self.env}
        result = subprocess.run(
            ["java", "-classpath", path.join(self.project_root, 'jar', 'z390.jar'),
             '-Xrs', 'ez390', asm_file_path, *args],
            capture_output=True,
            env=full_env
        )

        # capture the output to std
        self.stdout += result.stdout.decode()
        self.stderr += result.stderr.decode()

        return result.returncode

    def print_output(self):
        print(("*" * 20) + " stdout " + ("*" * 20))
        for line_num, line in enumerate(self.stdout.splitlines()):
            print("{:04}  {}".format(line_num, line))

        print(("*" * 20) + " stderr " + ("*" * 20))
        for line_num, line in enumerate(self.stderr.splitlines()):
            print("{:04}  {}".format(line_num, line))

        for file_ext in self.file_output:
            print(("*" * 20) + " ext: " + file_ext + " " + ("*" * 20))
            for line_num, line in enumerate(self.file_output[file_ext].splitlines()):
                print("{:04}  {}".format(line_num, line))

 */