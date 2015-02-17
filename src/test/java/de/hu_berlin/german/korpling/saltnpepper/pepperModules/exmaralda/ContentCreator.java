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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda;

import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusDocumentRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

public class ContentCreator 
{

	public static void createCorpus1(SaltProject saltProject)
	{
		createCorpusStructure1(saltProject);
		SElementId sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId("corp1/doc1");
		SDocument doc= saltProject.getSCorpusGraphs().get(0).getSDocuments().get(0);
		createDocumentStructure1(doc);
	}
	
	public static void createCorpusStructure1(SaltProject saltProject)
	{
		{//creating corpus structure
			SCorpusGraph corpGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
			saltProject.getSCorpusGraphs().add(corpGraph);
			//		corp1
			//		|
			//		doc1
			
			//corp1
			SElementId sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
			sElementId.setSId("corp1");
			SCorpus corp1= SaltCommonFactory.eINSTANCE.createSCorpus();
			corp1.setSName("corp1");
			corp1.setSElementId(sElementId);
			corpGraph.getSCorpora().add(corp1);
			
			//doc1
			SDocument doc1= SaltCommonFactory.eINSTANCE.createSDocument();
			sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
			sElementId.setSId("corp1/doc1");
			doc1.setSElementId(sElementId);
			doc1.setSName("doc1");
			corpGraph.getSDocuments().add(doc1);
			
			//CorpDocRel
			SCorpusDocumentRelation corpDocRel1= SaltCommonFactory.eINSTANCE.createSCorpusDocumentRelation();
			sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
			sElementId.setSId("rel1");
			corpDocRel1.setSElementId(sElementId);
			corpDocRel1.setSName("rel1");
			corpDocRel1.setSCorpus(corp1);
			corpDocRel1.setSDocument(doc1);
			corpGraph.getSCorpusDocumentRelations().add(corpDocRel1);
		}
	}
	
	public static void createCorpusStructure2(SaltProject saltProject)
	{
		SCorpusGraph corpGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
		saltProject.getSCorpusGraphs().add(corpGraph);
		//			corp2
		//		/		\
		//		doc1	doc2
		
		//corp2
		SElementId sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId("corp2");
		SCorpus corp2= SaltCommonFactory.eINSTANCE.createSCorpus();
		corp2.setSName("corp2");
		corp2.setSElementId(sElementId);
		corpGraph.getSCorpora().add(corp2);
		
		//doc1
		SDocument doc1= SaltCommonFactory.eINSTANCE.createSDocument();
		sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId("corp2/doc1");
		doc1.setSElementId(sElementId);
		doc1.setSName("doc1");
		corpGraph.getSDocuments().add(doc1);
		
		//CorpDocRel
		SCorpusDocumentRelation corpDocRel1= SaltCommonFactory.eINSTANCE.createSCorpusDocumentRelation();
		sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId("rel1");
		corpDocRel1.setSElementId(sElementId);
		corpDocRel1.setSName("rel1");
		corpDocRel1.setSCorpus(corp2);
		corpDocRel1.setSDocument(doc1);
		corpGraph.getSCorpusDocumentRelations().add(corpDocRel1);
		
		//doc2
		SDocument doc2= SaltCommonFactory.eINSTANCE.createSDocument();
		sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId("corp2/doc2");
		doc2.setSElementId(sElementId);
		doc2.setSName("doc2");
		corpGraph.getSDocuments().add(doc2);
		
		//CorpDocRel
		SCorpusDocumentRelation corpDocRel2= SaltCommonFactory.eINSTANCE.createSCorpusDocumentRelation();
		sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId("rel2");
		corpDocRel2.setSElementId(sElementId);
		corpDocRel2.setSName("rel2");
		corpDocRel2.setSCorpus(corp2);
		corpDocRel2.setSDocument(doc2);
		corpGraph.getSCorpusDocumentRelations().add(corpDocRel2);
	}
	
	
	public static void createDocumentStructure1(SDocument doc1)
	{
		if (doc1== null)
			throw new NullPointerException("Document is empty.");
		SElementId sElementId= null;
		
		//document content
		doc1.setSDocumentGraph(SaltCommonFactory.eINSTANCE.createSDocumentGraph());
		doc1.getSDocumentGraph().setSName(doc1.getSName()+ "_graph");
		//text
		STextualDS text1= SaltCommonFactory.eINSTANCE.createSTextualDS();
		text1.setSName("text1");
		SElementId textId= SaltCommonFactory.eINSTANCE.createSElementId();
		textId.setSId("text1");
		text1.setSText("This is a sample text.");
		doc1.getSDocumentGraph().addSNode(text1);
		
		//tokens and relations to text
		
		SToken tok = null;
		STextualRelation textRel= null;
		
		//tok1
		tok = SaltCommonFactory.eINSTANCE.createSToken();
		tok.setSName("tok1");
		sElementId = SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId(tok.getSName());
		tok.setSElementId(sElementId);
		doc1.getSDocumentGraph().addSNode(tok);
		//tok1 -> text1
		textRel= SaltCommonFactory.eINSTANCE.createSTextualRelation();
		textRel.setSToken(tok);
		textRel.setSTextualDS(text1);
		textRel.setSStart(0);
		textRel.setSEnd(5);
		doc1.getSDocumentGraph().addSRelation(textRel);
		
		//tok2
		tok = SaltCommonFactory.eINSTANCE.createSToken();
		tok.setSName("tok2");
		sElementId = SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId(tok.getSName());
		tok.setSElementId(sElementId);
		doc1.getSDocumentGraph().addSNode(tok);
		//tok2 -> text1
		textRel= SaltCommonFactory.eINSTANCE.createSTextualRelation();
		textRel.setSToken(tok);
		textRel.setSTextualDS(text1);
		textRel.setSStart(6);
		textRel.setSEnd(8);
		doc1.getSDocumentGraph().addSRelation(textRel);
		
		//tok3
		tok = SaltCommonFactory.eINSTANCE.createSToken();
		tok.setSName("tok3");
		sElementId = SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId(tok.getSName());
		tok.setSElementId(sElementId);
		doc1.getSDocumentGraph().addSNode(tok);
		//tok1 -> text1
		textRel= SaltCommonFactory.eINSTANCE.createSTextualRelation();
		textRel.setSToken(tok);
		textRel.setSTextualDS(text1);
		textRel.setSStart(9);
		textRel.setSEnd(10);
		doc1.getSDocumentGraph().addSRelation(textRel);
		
		//tok4
		tok = SaltCommonFactory.eINSTANCE.createSToken();
		tok.setSName("tok4");
		sElementId = SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId(tok.getSName());
		tok.setSElementId(sElementId);
		doc1.getSDocumentGraph().addSNode(tok);
		//tok1 -> text1
		textRel= SaltCommonFactory.eINSTANCE.createSTextualRelation();
		textRel.setSToken(tok);
		textRel.setSTextualDS(text1);
		textRel.setSStart(11);
		textRel.setSEnd(17);
		doc1.getSDocumentGraph().addSRelation(textRel);
		
		//tok5
		tok = SaltCommonFactory.eINSTANCE.createSToken();
		tok.setSName("tok5");
		sElementId = SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId(tok.getSName());
		tok.setSElementId(sElementId);
		doc1.getSDocumentGraph().addSNode(tok);
		//tok1 -> text1
		textRel= SaltCommonFactory.eINSTANCE.createSTextualRelation();
		textRel.setSToken(tok);
		textRel.setSTextualDS(text1);
		textRel.setSStart(18);
		textRel.setSEnd(22);
		doc1.getSDocumentGraph().addSRelation(textRel);
		
		//tok6
		tok = SaltCommonFactory.eINSTANCE.createSToken();
		tok.setSName("tok6");
		sElementId = SaltCommonFactory.eINSTANCE.createSElementId();
		sElementId.setSId(tok.getSName());
		tok.setSElementId(sElementId);
		doc1.getSDocumentGraph().addSNode(tok);
		//tok1 -> text1
		textRel= SaltCommonFactory.eINSTANCE.createSTextualRelation();
		textRel.setSToken(tok);
		textRel.setSTextualDS(text1);
		textRel.setSStart(22);
		textRel.setSEnd(23);
		doc1.getSDocumentGraph().addSRelation(textRel);
		
		//create Annotations
		for (SToken sToken: doc1.getSDocumentGraph().getSTokens())
		{
			SAnnotation posAnno= SaltCommonFactory.eINSTANCE.createSAnnotation();
			posAnno.setSName("pos");
			posAnno.setSValue("anyPosAnno");
			sToken.addSAnnotation(posAnno);
			
			SAnnotation lemmaAnno= SaltCommonFactory.eINSTANCE.createSAnnotation();
			lemmaAnno.setSName("lemma");
			lemmaAnno.setSValue("anyLemmaAnno");
			sToken.addSAnnotation(lemmaAnno);
		}	
	}
}
