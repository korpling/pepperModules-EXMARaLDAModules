/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.exmaralda;

import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusDocumentRelation;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.graph.Identifier;

public class ContentCreator 
{

	public static void createCorpus1(SaltProject saltProject)
	{
		createCorpusStructure1(saltProject);
		SDocument doc= saltProject.getCorpusGraphs().get(0).getDocuments().get(0);
		createDocumentStructure1(doc);
	}
	
	public static void createCorpusStructure1(SaltProject saltProject)
	{
		{//creating corpus structure
			SCorpusGraph corpGraph= SaltFactory.createSCorpusGraph();
			saltProject.addCorpusGraph(corpGraph);
			//		corp1
			//		|
			//		doc1
			
			//corp1
			SCorpus corp1= SaltFactory.createSCorpus();
			corp1.setName("corp1");
			SaltFactory.createIdentifier(corp1, "corp1");
			corpGraph.getCorpora().add(corp1);
			
			//doc1
			SDocument doc1= SaltFactory.createSDocument();
			SaltFactory.createIdentifier(corp1, "corp1/doc1");
			doc1.setName("doc1");
			corpGraph.getDocuments().add(doc1);
			
			//CorpDocRel
			SCorpusDocumentRelation corpDocRel1= SaltFactory.createSCorpusDocumentRelation();
			SaltFactory.createIdentifier(corpDocRel1, "rel1");
			corpDocRel1.setName("rel1");
			corpDocRel1.setSource(corp1);
			corpDocRel1.setTarget(doc1);
			corpGraph.addRelation(corpDocRel1);
		}
	}
	
	public static void createCorpusStructure2(SaltProject saltProject)
	{
		SCorpusGraph corpGraph= SaltFactory.createSCorpusGraph();
		saltProject.addCorpusGraph(corpGraph);
		//			corp2
		//		/		\
		//		doc1	doc2
		
		//corp2
		SCorpus corp2= SaltFactory.createSCorpus();
		corp2.setName("corp2");
		SaltFactory.createIdentifier(corp2, "corp2");
		corpGraph.getCorpora().add(corp2);
		
		//doc1
		SDocument doc1= SaltFactory.createSDocument();
		SaltFactory.createIdentifier(doc1, "corp2/doc1");
		doc1.setName("doc1");
		corpGraph.getDocuments().add(doc1);
		
		//CorpDocRel
		SCorpusDocumentRelation corpDocRel1= SaltFactory.createSCorpusDocumentRelation();
		SaltFactory.createIdentifier(corpDocRel1, "rel1");
		corpDocRel1.setName("rel1");
		corpDocRel1.setSource(corp2);
		corpDocRel1.setTarget(doc1);
		corpGraph.getCorpusDocumentRelations().add(corpDocRel1);
		
		//doc2
		SDocument doc2= SaltFactory.createSDocument();
		SaltFactory.createIdentifier(doc2, "corp2/doc2");
		doc2.setName("doc2");
		corpGraph.getDocuments().add(doc2);
		
		//CorpDocRel
		SCorpusDocumentRelation corpDocRel2= SaltFactory.createSCorpusDocumentRelation();
		SaltFactory.createIdentifier(corpDocRel2, "rel2");
		corpDocRel2.setName("rel2");
		corpDocRel2.setSource(corp2);
		corpDocRel2.setTarget(doc2);
		corpGraph.getCorpusDocumentRelations().add(corpDocRel2);
	}
	
	
	public static void createDocumentStructure1(SDocument doc1)
	{
		if (doc1== null){
			throw new NullPointerException("Document is empty.");
		}
		Identifier sElementId= null;
		
		//document content
		doc1.setDocumentGraph(SaltFactory.createSDocumentGraph());
		doc1.getDocumentGraph().setName(doc1.getName()+ "_graph");
		//text
		STextualDS text1= SaltFactory.createSTextualDS();
		text1.setName("text1");
		text1.setText("This is a sample text.");
		doc1.getDocumentGraph().addNode(text1);
		
		//tokens and relations to text
		
		SToken tok = null;
		STextualRelation textRel= null;
		
		//tok1
		tok = SaltFactory.createSToken();
		tok.setName("tok1");
		SaltFactory.createIdentifier(tok, tok.getName());
		doc1.getDocumentGraph().addNode(tok);
		//tok1 -> text1
		textRel= SaltFactory.createSTextualRelation();
		textRel.setSource(tok);
		textRel.setTarget(text1);
		textRel.setStart(0);
		textRel.setEnd(5);
		doc1.getDocumentGraph().addRelation(textRel);
		
		//tok2
		tok = SaltFactory.createSToken();
		tok.setName("tok2");
		SaltFactory.createIdentifier(tok, tok.getName());
		doc1.getDocumentGraph().addNode(tok);
		//tok2 -> text1
		textRel= SaltFactory.createSTextualRelation();
		textRel.setSource(tok);
		textRel.setTarget(text1);
		textRel.setStart(6);
		textRel.setEnd(8);
		doc1.getDocumentGraph().addRelation(textRel);
		
		//tok3
		tok = SaltFactory.createSToken();
		tok.setName("tok3");
		SaltFactory.createIdentifier(tok, tok.getName());
		doc1.getDocumentGraph().addNode(tok);
		//tok1 -> text1
		textRel= SaltFactory.createSTextualRelation();
		textRel.setSource(tok);
		textRel.setTarget(text1);
		textRel.setStart(9);
		textRel.setEnd(10);
		doc1.getDocumentGraph().addRelation(textRel);
		
		//tok4
		tok = SaltFactory.createSToken();
		tok.setName("tok4");
		SaltFactory.createIdentifier(tok, tok.getName());
		doc1.getDocumentGraph().addNode(tok);
		//tok1 -> text1
		textRel= SaltFactory.createSTextualRelation();
		textRel.setSource(tok);
		textRel.setTarget(text1);
		textRel.setStart(11);
		textRel.setEnd(17);
		doc1.getDocumentGraph().addRelation(textRel);
		
		//tok5
		tok = SaltFactory.createSToken();
		tok.setName("tok5");
		SaltFactory.createIdentifier(tok, tok.getName());
		doc1.getDocumentGraph().addNode(tok);
		//tok1 -> text1
		textRel= SaltFactory.createSTextualRelation();
		textRel.setSource(tok);
		textRel.setTarget(text1);
		textRel.setStart(18);
		textRel.setEnd(22);
		doc1.getDocumentGraph().addRelation(textRel);
		
		//tok6
		tok = SaltFactory.createSToken();
		tok.setName("tok6");
		SaltFactory.createIdentifier(tok, tok.getName());
		doc1.getDocumentGraph().addNode(tok);
		//tok1 -> text1
		textRel= SaltFactory.createSTextualRelation();
		textRel.setSource(tok);
		textRel.setTarget(text1);
		textRel.setStart(22);
		textRel.setEnd(23);
		doc1.getDocumentGraph().addRelation(textRel);
		
		//create Annotations
		for (SToken sToken: doc1.getDocumentGraph().getTokens())
		{
			SAnnotation posAnno= SaltFactory.createSAnnotation();
			posAnno.setName("pos");
			posAnno.setValue("anyPosAnno");
			sToken.addAnnotation(posAnno);
			
			SAnnotation lemmaAnno= SaltFactory.createSAnnotation();
			lemmaAnno.setName("lemma");
			lemmaAnno.setValue("anyLemmaAnno");
			sToken.addAnnotation(lemmaAnno);
		}	
	}
}
