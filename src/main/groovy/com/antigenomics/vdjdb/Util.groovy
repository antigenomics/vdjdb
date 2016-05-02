/*
 * Copyright 2015 Mikhail Shugay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.antigenomics.vdjdb

import com.milaboratory.core.sequence.AminoAcidSequence
import groovy.transform.CompileStatic

class Util {
    static
    final String HOME_DIR = new File(Util.class.protectionDomain.codeSource.location.path).parent.replaceAll("%20", " ")

    static boolean checkDatabase(boolean updateIfNewer = false) {
        def version

        def versionFile = new File(HOME_DIR + "/latest-version.txt")
        if (!versionFile.exists()) {
            System.err.println("[VDJDB-update] No database is present (running for the first time, I assume), downloading it. " +
                    "NOTE: No automatic update will be performed next time VDJdb is run - use Update command to check for database updates.")
            version = ""
        } else {
            version = versionFile.readLines()[0]
        }

        if (!versionFile.exists() || updateIfNewer) {
            def latestVersion = new URL("https://raw.githubusercontent.com/antigenomics/vdjdb-db/master/latest-version.txt").openStream().readLines()[0]

            if (version != latestVersion) {
                versionFile << latestVersion
            }

            def dbZip = new File(HOME_DIR + "/vdjdb.zip")
            def out = new BufferedOutputStream(new FileOutputStream(dbZip))
            out << new URL(latestVersion).openStream()
            out.close()

            def ant = new AntBuilder()

            ant.unzip(src: dbZip.absolutePath,
                    dest: HOME_DIR,
                    overwrite: "true")

            System.err.println("[VDJDB-update] Done, you are now using ${latestVersion.split("/")[-2]}")

            return true
        }

        return false
    }

    static InputStream resourceAsStream(String resourceName) {
        Util.class.classLoader.getResourceAsStream(resourceName)
    }

    static String replaceNonAa(String seq) {
        seq.replaceAll(/[^FLSYCWPHQRIMTNKVADEG]/, "X")
    }

    static AminoAcidSequence convert(String aaSeq) {
        aaSeq = aaSeq.trim()
        if (aaSeq.length() > 0 && aaSeq =~ /^[FLSYCWPHQRIMTNKVADEG]+$/)
            return new AminoAcidSequence(aaSeq)

        //System.err.println("Error converting '$aaSeq' to amino acid sequences, entry will be skipped from search")
        null
    }

    static String simplifySegmentName(String segmentName) {
        segmentName = segmentName.split(",")[0] // take best match
        segmentName.split("\\*")[0] // trim allele if present
    }

    @CompileStatic
    static String codon2aa(String codon) {
        String codonUpper = codon.toUpperCase()
        switch (codonUpper) {
            case 'TTT': return 'F'
            case 'TTC': return 'F'
            case 'TTA': return 'L'
            case 'TTG': return 'L'
            case 'TCT': return 'S'
            case 'TCC': return 'S'
            case 'TCA': return 'S'
            case 'TCG': return 'S'
            case 'TAT': return 'Y'
            case 'TAC': return 'Y'
            case 'TAA': return '*'
            case 'TAG': return '*'
            case 'TGT': return 'C'
            case 'TGC': return 'C'
            case 'TGA': return '*'
            case 'TGG': return 'W'
            case 'CTT': return 'L'
            case 'CTC': return 'L'
            case 'CTA': return 'L'
            case 'CTG': return 'L'
            case 'CCT': return 'P'
            case 'CCC': return 'P'
            case 'CCA': return 'P'
            case 'CCG': return 'P'
            case 'CAT': return 'H'
            case 'CAC': return 'H'
            case 'CAA': return 'Q'
            case 'CAG': return 'Q'
            case 'CGT': return 'R'
            case 'CGC': return 'R'
            case 'CGA': return 'R'
            case 'CGG': return 'R'
            case 'ATT': return 'I'
            case 'ATC': return 'I'
            case 'ATA': return 'I'
            case 'ATG': return 'M'
            case 'ACT': return 'T'
            case 'ACC': return 'T'
            case 'ACA': return 'T'
            case 'ACG': return 'T'
            case 'AAT': return 'N'
            case 'AAC': return 'N'
            case 'AAA': return 'K'
            case 'AAG': return 'K'
            case 'AGT': return 'S'
            case 'AGC': return 'S'
            case 'AGA': return 'R'
            case 'AGG': return 'R'
            case 'GTT': return 'V'
            case 'GTC': return 'V'
            case 'GTA': return 'V'
            case 'GTG': return 'V'
            case 'GCT': return 'A'
            case 'GCC': return 'A'
            case 'GCA': return 'A'
            case 'GCG': return 'A'
            case 'GAT': return 'D'
            case 'GAC': return 'D'
            case 'GAA': return 'E'
            case 'GAG': return 'E'
            case 'GGT': return 'G'
            case 'GGC': return 'G'
            case 'GGA': return 'G'
            case 'GGG': return 'G'
            default:
                if (codonUpper.contains("N") && codonUpper.length() == 3)
                    return "X" // undefined
                else
                    return '?' // incomplete/missing
        }
    }

    @CompileStatic
    static String translateLinear(String seq, boolean reverse) {
        def aaSeq = ""

        if (reverse)
            seq = seq.substring(seq.length() % 3)

        for (int i = 0; i <= seq.size() - 3; i += 3) {
            def codon = seq.substring(i, i + 3)
            aaSeq += codon2aa(codon)
        }

        aaSeq
    }
}
