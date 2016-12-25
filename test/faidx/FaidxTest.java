package faidx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;

public class FaidxTest {

		
	@Test
	public void canIndexValidSeqs() throws IOException, UnindexableFastaFileException {
		
		File fasta= new File("test_data/faidx/indexable.fa");
		File fai= new File(fasta.getAbsoluteFile() + ".fai");
		fai.deleteOnExit();
		
		/* Expected index file created with samtools 1.3.1:
           samtools faidx indexable.fa
           mv indexable.fa.fai indexable.fa.fai.expected
		*/
		List<String> expected= Arrays.asList(FileUtils.readFileToString(new File("test_data/faidx/indexable.fa.fai.expected")).split("\n"));
		
		new Faidx(fasta);
		assertTrue(fai.isFile());
		
		List<String> observed= Arrays.asList(FileUtils.readFileToString(fai).split("\n"));
		
		assertEquals(expected.size(), observed.size());
		
		for(int i= 0; i < expected.size(); i++){
			assertEquals(expected.get(i), observed.get(i));
		}
	}

	@Test
	public void canHandleWindowsLineEndings() throws IOException, UnindexableFastaFileException{

		File fasta= new File("test_data/faidx/indexable.crlf.fa");
		File fai= new File(fasta.getAbsoluteFile() + ".fai");
		fai.deleteOnExit();
		
		/* Expected index file created with samtools 1.3.1:
           samtools faidx indexable.crlf.fa
           mv indexable.crlf.fa.fai indexable.crlf.fa.fai.expected
		*/
		List<String> expected= Arrays.asList(FileUtils.readFileToString(new File("test_data/faidx/indexable.crlf.fa.fai.expected")).split("\n"));
		
		new Faidx(fasta);
		assertTrue(fai.isFile());
		
		List<String> observed= Arrays.asList(FileUtils.readFileToString(fai).split("\n"));
		
		assertEquals(expected.size(), observed.size());
		
		for(int i= 0; i < expected.size(); i++){
			assertEquals(expected.get(i), observed.get(i));
		}

		
	}
	
	@Test
	public void canHandleEmptySequence() throws IOException, UnindexableFastaFileException{
		File fasta= new File("test_data/faidx/empty.fa");
		File fai= new File(fasta.getAbsoluteFile() + ".fai");
		fai.deleteOnExit();
		
		new Faidx(fasta);
		
		List<String> observed= Arrays.asList(FileUtils.readFileToString(fai).split("\n"));
		for(String x : observed){
			if(x.startsWith("empty")){ // Empty sequences just check sequence length is 0
				assertEquals("0", x.split("\t")[1]);
			}
		}
		
		// Can retrieve seqs
		IndexedFastaSequenceFile ref= new IndexedFastaSequenceFile(new File("test_data/faidx/empty.fa"));
		assertEquals("ACTGNNNNNNNNNNNNN", new String(ref.getSubsequenceAt("seq", 1, 17).getBases()));
		assertEquals("AACCGGTTNN", new String(ref.getSubsequenceAt("seq2", 1, 10).getBases()));
		assertEquals("GGGAAATTTNNNCCC", new String(ref.getSubsequenceAt("seq3", 1, 15).getBases()));
		
		ref.close();
	}
	
	@Test(expected = UnindexableFastaFileException.class) 
	public void exceptionOnDuplicateName() throws IOException, UnindexableFastaFileException{
		File fasta= new File("test_data/faidx/dups.fa");
		new Faidx(fasta);
		assertTrue( ! new File(fasta.getAbsolutePath() + ".fai").exists());
	}

	@Test(expected = UnindexableFastaFileException.class) 
	public void exceptionOnDifferentLineLength() throws IOException, UnindexableFastaFileException{
		File fasta= new File("test_data/faidx/lineLen.fa");
		new Faidx(fasta);
		assertTrue( ! new File(fasta.getAbsolutePath() + ".fai").exists());
	}
	
	@Test(expected = UnindexableFastaFileException.class)
	public void exceptionOnGzipInput() throws IOException, UnindexableFastaFileException{
		File fasta= new File("test_data/faidx/indexable.fa.gz");
		new Faidx(fasta);
		assertTrue( ! new File(fasta.getAbsolutePath() + ".fai").exists());
	}
	
}
