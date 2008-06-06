/**=================================================================================================================
* File Name       : CDCXForms.java
* Author          : Suresh Chandra Pal @ Endeavour Software, Bangalore.
* Date            : 03/27/2008
* Description     : This class is used to create a XForm Prototype.
* History         :
* ==================================================================================================================
*  Sr. No.  Date        Name                                Reviewed By             Description
* ==================================================================================================================
*  01.      03-27-08    Suresh Chandra Pal @ Endeavour
* ==================================================================================================================
*/

package org.javarosa.dtree.storage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.javarosa.clforms.api.Form;
import org.javarosa.clforms.storage.Externalizable;
import org.javarosa.clforms.storage.IDRecordable;
import org.javarosa.clforms.storage.XFormRMSUtility;
import org.javarosa.clforms.xml.XMLUtil;

import java.util.*;

public class CDCXFormDemo implements Externalizable,IDRecordable {

        public static Vector xFormStore = new Vector();

        private XFormRMSUtility xformRMS;

        private static byte[] cdcXForm ;
        /**
         * Header start for XForm
         */
        private static String xForm = new String("" +
                    // xFormHeaderStart
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:xf=\"http://www.w3.org/2002/xforms\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\r\n" +
                    "   <head>\r\n" +
                    
                    // modelStart
                    "       <title>CDC Demo</title>\r\n" +
                    "       <xf:model id=\"CDCDemo\">\r\n" +
                    // iTextNodeStart
                    "           <itext xmlns=\"\">\r\n" +
                    
                    // Language1 Translation Tag 
                    "               <translation lang=\"English\" default =\"\" >\r\n" +
                    // Language1 QuestionOne
                    "                   <text id=\"question1\"> \r\n" +
                    "                       <value form=\"long\">Eng: Enter ARV Regimen</value> \r\n" +
                    "                       <value form=\"short\">Eng : Rash?</value> \r\n" +
                    "                       <value form=\"hint\">Eng : Select your Regimen</value> \r\n" +
                    "                   </text> \r\n" +
               
                    "                   <text id=\"queOneLabel1\"> \r\n" +
                    "                       <value form=\"\">Eng : 1a30:d4T30-3TC-NVP</value> \r\n" +
                    "                   </text> \r\n"  +

                    "                   <text id=\"queOneLabel2\"> \r\n" +
                    "                       <value form=\"\">Eng : 1a40:d4T30-3TC-NVP</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queOneLabel3\"> \r\n" +
                    "                       <value form=\"\">Eng : 1a30-S: starting</value> \r\n" +
                    "                   </text> \r\n" +
                        
                    "                   <text id=\"queOneLabel4\"> \r\n" +
                    "                       <value form=\"\">Eng : 1a40-S: starting</value> \r\n" +
                    "                   </text> \r\n" +
                    
                    "                        <text id=\"queOneLabel5\"> \r\n" +
                    "                            <value form=\"\">Eng : 1b:ZDV-CTC-NVP</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel6\"> \r\n" +
                    "                            <value form=\"\">Eng : 1c:ZDV-3TC-EFV</value> \r\n" +
                    "                        </text> \r\n" +
                    
                    "                        <text id=\"queOneLabel7\"> \r\n" +
                    "                            <value form=\"\">Eng : 1d40:d4T40-3TC-EFV</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel8\"> \r\n" +
                    "                            <value form=\"\">Eng : 2b:ABC-ddl-SQV/r</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel9\"> \r\n" +
                    "                            <value form=\"\">Eng : 2a:ABC-ddl-LPV/r</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel10\"> \r\n" +
                    "                            <value form=\"\">Eng : 2c:ABC-ddl-NFV</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel11\"> \r\n" +
                    "                            <value form=\"\">Eng : Other regimen</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel12\"> \r\n" +
                    "                            <value form=\"\">Eng : Not on Arvs Yet</value> \r\n" +
                    "                        </text> \r\n"  +
                    // Language2 QuestionTwo
                    "                   <text id=\"question2\"> \r\n" +
                    "                       <value form=\"long\">Eng : ASK: The Records indicate that your current regimen is 1c:ZDV-3TC-EFV. Has this changed?</value> \r\n" +
                    "                       <value form=\"short\">Eng : Regimen Changed</value> \r\n" +
                    "			    <value form=\"hint\">Eng : Select your Current Regimen</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queTwoLabel1\"> \r\n" +
                    "                       <value form=\"\">Eng : Switch entry to 1c:ZDV-3TC-EFV</value> \r\n" +
                    "                   </text> \r\n"  +

                    "                   <text id=\"queTwoLabel2\"> \r\n" +
                    "                       <value form=\"\">Eng : Keep Entry</value> \r\n" +
                    "                   </text> \r\n" +  
                    // Language3 QuestionThree
                    "                        <text id=\"question3\"> \r\n" +
                    "                            <value form=\"long\">Eng : ASK: Our records indicate you have been on treatment for 12 months. Is that correct?</value> \r\n" +
                    "                            <value form=\"short\">Eng : Treatment Status</value> \r\n" +
                    "			    <value form=\"hint\">Eng : Answer yes if the above is correct.</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queThreeLabel1\"> \r\n" +
                    "                            <value form=\"\">Eng : YES</value> \r\n" +
                    "                        </text> \r\n"  +

                    "                        <text id=\"queThreeLabel2\"> \r\n" +
                    "                            <value form=\"\">Eng : NO</value> \r\n" +
                    "                        </text> \r\n"  +
                
                    // Language1 Question4
                    "                   <text id=\"question4\"> \r\n" +
                    "                       <value form=\"long\">Eng : Remember to order the patients 12 months CD4 test!</value> \r\n" +
                    "                       <value form=\"short\">Eng : Lab Test Order</value> \r\n" +
                    "			    <value form=\"hint\">Eng : Press button</value> \r\n" +
                    "                   </text> \r\n" +
                    
                    // Language1 Question5
                    "                   <text id=\"question5_1\"> \r\n" +
                    "                       <value form=\"long\">Eng : Enter Weight</value> \r\n" +
                    "                       <value form=\"short\">Eng : Weight</value> \r\n" +
                    "			    <value form=\"hint\">Eng : Enter Weight</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"question5_2\"> \r\n" +
                    "                       <value form=\"long\">Eng : Units</value> \r\n" +
                    "                       <value form=\"short\">Eng : Units</value> \r\n" +
                    "			    <value form=\"hint\">Eng : Select Units</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queFiveLabel1\"> \r\n" +
                    "                       <value form=\"\">Eng : Kilograms</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queFiveLabel2\"> \r\n" +
                    "                       <value form=\"\">Eng : Pounds</value> \r\n" +
                    "                   </text> \r\n" + 
                
                    // Language1 Question6
                    "                   <text id=\"question6\"> \r\n" +
                    "                       <value form=\"long\">Eng : Weight Chart</value> \r\n" +
                    "                       <value form=\"short\">Eng : Weight Chart</value> \r\n" +
                    "			    <value form=\"hint\">Eng : Weight Chart</value> \r\n" +
                    "                   </text> \r\n" +                 
                    // Language1 Question7
                   "                        <text id=\"question7\"> \r\n" +
                   "                            <value form=\"long\">Eng : TB Status</value> \r\n" +
                   "                            <value form=\"short\">Eng : TB Status</value> \r\n" +
                   "			    <value form=\"hint\">Eng : Select TB Status</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel1\"> \r\n" +
                   "                            <value form=\"\">Eng : NO TB treatment or signs</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel2\"> \r\n" +
                   "                            <value form=\"\">Eng : Waiting for TB results</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel3\"> \r\n" +
                   "                            <value form=\"\">Eng : On INH Prophylaxis</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel4\"> \r\n" +
                   "                            <value form=\"\">Eng : TB confirmed, will start treatment</value> \r\n" +
                   "                        </text> \r\n" +
                   // Language1 Question8
                       "                        <text id=\"question8\"> \r\n" +
                       "                            <value form=\"long\">Eng : Now I will ask you some questions to determine if you need to see a doctor on this visit.</value> \r\n" +
                       "                            <value form=\"short\">Eng : Questions</value> \r\n" +
                       "			    <value form=\"hint\">Eng : Answer Correctly</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Language1 Question9
                       "                        <text id=\"question9\"> \r\n" +
                       "                            <value form=\"long\">Eng : Have You been admitted to hospital since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">Eng : Admission Status</value> \r\n" +
                       "			    <value form=\"hint\">Eng : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel1\"> \r\n" +
                       "                            <value form=\"\">Eng : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel2\"> \r\n" +
                       "                            <value form=\"\">Eng : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language1 Question10
                       "                        <text id=\"question10\"> \r\n" +
                       "                            <value form=\"long\">Eng : Do you have any rash?</value> \r\n" +
                       "                            <value form=\"short\">Eng : Rash</value> \r\n" +
                       "			    <value form=\"hint\">Eng : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel1\"> \r\n" +
                       "                            <value form=\"\">Eng : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel2\"> \r\n" +
                       "                            <value form=\"\">Eng : YES</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Language1 Question11
                       "                        <text id=\"question11\"> \r\n" +
                       "                            <value form=\"long\">Eng : Is it new or getting worse since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">Eng : Status after last visit</value> \r\n" +
                       "			    <value form=\"hint\">Eng : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel1\"> \r\n" +
                       "                            <value form=\"\">Eng : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel2\"> \r\n" +
                       "                            <value form=\"\">Eng : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       "            </translation>\r\n"+
                // Language2 Translation tag
                       "            <translation lang=\"Swahili\" >\r\n" +
                       // Language2 Question1
                       "                        <text id=\"question1\"> \r\n" +
                       "                            <value form=\"long\">SW: Enter ARV Regimen</value> \r\n" +
                       "                            <value form=\"short\">SW : Rash?</value> \r\n" +
                       "			    <value form=\"hint\">SW : Select your Regimen</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel1\"> \r\n" +
                       "                            <value form=\"\"></value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : 1a40:d4T30-3TC-NVP</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel3\"> \r\n" +
                       "                            <value form=\"\">SW : 1a30-S: starting</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel4\"> \r\n" +
                       "                            <value form=\"\">SW : 1a40-S: starting</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel5\"> \r\n" +
                       "                            <value form=\"\">SW : 1b:ZDV-CTC-NVP</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel6\"> \r\n" +
                       "                            <value form=\"\">SW : 1c:ZDV-3TC-EFV</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel7\"> \r\n" +
                       "                            <value form=\"\">SW : 1d40:d4T40-3TC-EFV</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel8\"> \r\n" +
                       "                            <value form=\"\">SW : 2b:ABC-ddl-SQV/r</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel9\"> \r\n" +
                       "                            <value form=\"\">SW : 2a:ABC-ddl-LPV/r</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel10\"> \r\n" +
                       "                            <value form=\"\">SW : 2c:ABC-ddl-NFV</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel11\"> \r\n" +
                       "                            <value form=\"\">SW : Other regimen</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel12\"> \r\n" +
                       "                            <value form=\"\">SW : Not on Arvs Yet</value> \r\n" +
                       "                        </text> \r\n" +                
                       // Language2 Question2
                       "                        <text id=\"question2\"> \r\n" +
                       "                            <value form=\"long\">SW : ASK: The Records indicate that your current regimen is 1c:ZDV-3TC-EFV. Has this changed?</value> \r\n" +
                       "                            <value form=\"short\">SW : Regimen Changed</value> \r\n" +
                       "			    <value form=\"hint\">SW : Select your Current Regimen</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queTwoLabel1\"> \r\n" +
                       "                            <value form=\"\">SW : Switch entry to 1c:ZDV-3TC-EFV</value> \r\n" +
                       "                        </text> \r\n"  +
                
                       "                        <text id=\"queTwoLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : Keep Entry</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Language2 Question3
                       "                        <text id=\"question3\"> \r\n" +
                       "                            <value form=\"long\">SW : ASK: Our records indicate you have been on treatment for 12 months. Is that correct?</value> \r\n" +
                       "                            <value form=\"short\">SW : Treatment Status</value> \r\n" +
                       "			    <value form=\"hint\">SW : Answer yes if the above is correct.</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queThreeLabel1\"> \r\n" +
                       "                            <value form=\"\">SW : YES</value> \r\n" +
                       "                        </text> \r\n"  +
                
                       "                        <text id=\"queThreeLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : NO</value> \r\n" +
                       "                        </text> \r\n"  +
                       // Language2 Question4
                       "                        <text id=\"question4\"> \r\n" +
                       "                            <value form=\"long\">SW : Remember to order the patients 12 months CD4 test!</value> \r\n" +
                       "                            <value form=\"short\">SW : Lab Test Order</value> \r\n" +
                       "			    <value form=\"hint\">SW : Press button</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language3 Question5
                       "                        <text id=\"question5_1\"> \r\n" +
                       "                            <value form=\"long\">SW : Enter Weight</value> \r\n" +
                       "                            <value form=\"short\">SW : Weight</value> \r\n" +
                       "			    <value form=\"hint\">SW : Enter Weight</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"question5_2\"> \r\n" +
                       "                            <value form=\"long\">SW : Units</value> \r\n" +
                       "                            <value form=\"short\">SW : Units</value> \r\n" +
                       "			    <value form=\"hint\">SW : Select Units</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queFiveLabel1\"> \r\n" +
                       "                            <value form=\"\">SW : Kilograms</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queFiveLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : Pounds</value> \r\n" +
                       "                        </text> \r\n" +
                       
                       // Language2 Question6
                       "                        <text id=\"question6\"> \r\n" +
                       "                            <value form=\"long\">SW : Weight Chart</value> \r\n" +
                       "                            <value form=\"short\">SW : Weight Chart</value> \r\n" +
                       "			    <value form=\"hint\">SW : Weight Chart</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Language2 Question7
                       "                        <text id=\"question7\"> \r\n" +
                       "                            <value form=\"long\">SW : TB Status</value> \r\n" +
                       "                            <value form=\"short\">SW : TB Status</value> \r\n" +
                       "			    <value form=\"hint\">SW : Select TB Status</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queSevenLabel1\"> \r\n" +
                       "                            <value form=\"\">SW : NO TB treatment or signs</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queSevenLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : Waiting for TB results</value> \r\n" +
                       "                        </text> \r\n" +
                       
                       "                        <text id=\"queSevenLabel3\"> \r\n" +
                       "                            <value form=\"\">SW : On INH Prophylaxis</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queSevenLabel4\"> \r\n" +
                       "                            <value form=\"\">SW : TB confirmed, will start treatment</value> \r\n" +
                       "                        </text> \r\n" +
                       
                       // Language2 Question8
                       
                       "                        <text id=\"question8\"> \r\n" +
                       "                            <value form=\"long\">SW : Now I will ask you some questions to determine if you need to see a doctor on this visit.</value> \r\n" +
                       "                            <value form=\"short\">SW : Questions</value> \r\n" +
                       "			    <value form=\"hint\">SW : Answer Correctly</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language2 Question9
                       "                        <text id=\"question9\"> \r\n" +
                       "                            <value form=\"long\">SW : Have You been admitted to hospital since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">SW : Admission Status</value> \r\n" +
                       "			    <value form=\"hint\">SW : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel1\"> \r\n" +
                       "                            <value form=\"\">SW : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language2 Question10
                       "                        <text id=\"question10\"> \r\n" +
                       "                            <value form=\"long\">SW : Do you have any rash?</value> \r\n" +
                       "                            <value form=\"short\">SW : Rash</value> \r\n" +
                       "			    <value form=\"hint\">SW : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel1\"> \r\n" +
                       "                            <value form=\"\">SW : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language2 Question11
                       "                        <text id=\"question11\"> \r\n" +
                       "                            <value form=\"long\">SW : Is it new or getting worse since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">SW : Status after last visit</value> \r\n" +
                       "			    <value form=\"hint\">SW : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel1\"> \r\n" +
                       "                            <value form=\"\">SW : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel2\"> \r\n" +
                       "                            <value form=\"\">SW : YES</value> \r\n" +
                       "                        </text> \r\n" +                
                
                       "            </translation>\r\n"+
                
                       // Language3 Translation tag
                       "            <translation lang=\"Japanese\" >\r\n" +
                       // Language3 Question1
                       "                        <text id=\"question1\"> \r\n" +
                       "                            <value form=\"long\"></value> \r\n" +
                       "                            <value form=\"short\"></value> \r\n" +
                       "			    <value form=\"hint\"></value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : 1a30:d4T30-3TC-NVP</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : 1a40:d4T30-3TC-NVP</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel3\"> \r\n" +
                       "                            <value form=\"\">JA : 1a30-S: starting</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel4\"> \r\n" +
                       "                            <value form=\"\">JA : 1a40-S: starting</value> \r\n" +
                       "                        </text> \r\n" +
                
                       "                        <text id=\"queOneLabel5\"> \r\n" +
                       "                            <value form=\"\">JA : 1b:ZDV-CTC-NVP</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel6\"> \r\n" +
                       "                            <value form=\"\">JA : 1c:ZDV-3TC-EFV</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel7\"> \r\n" +
                       "                            <value form=\"\">JA : 1d40:d4T40-3TC-EFV</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel8\"> \r\n" +
                       "                            <value form=\"\">JA : 2b:ABC-ddl-SQV/r</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel9\"> \r\n" +
                       "                            <value form=\"\">JA : 2a:ABC-ddl-LPV/r</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel10\"> \r\n" +
                       "                            <value form=\"\">JA : 2c:ABC-ddl-NFV</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel11\"> \r\n" +
                       "                            <value form=\"\">JA : Other regimen</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queOneLabel12\"> \r\n" +
                       "                            <value form=\"\">JA : Not on Arvs Yet</value> \r\n" +
                       "                        </text> \r\n" +  
                
                
                       // Language3 Question2
                       "                        <text id=\"question2\"> \r\n" +
                       "                            <value form=\"long\">JA : ASK: The Records indicate that your current regimen is 1c:ZDV-3TC-EFV. Has this changed?</value> \r\n" +
                       "                            <value form=\"short\">JA : Regimen Changed</value> \r\n" +
                       "			    <value form=\"hint\">JA : Select your Current Regimen</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queTwoLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : Switch entry to 1c:ZDV-3TC-EFV</value> \r\n" +
                       "                        </text> \r\n"  +
                
                       "                        <text id=\"queTwoLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : Keep Entry</value> \r\n" +
                       "                        </text> \r\n"  +
                       // Language3 Question3
                       "                        <text id=\"question3\"> \r\n" +
                       "                            <value form=\"long\">JA : ASK: Our records indicate you have been on treatment for 12 months. Is that correct?</value> \r\n" +
                       "                            <value form=\"short\">JA : Treatment Status</value> \r\n" +
                       "			    <value form=\"hint\">JA : Answer yes if the above is correct.</value> \r\n" +
                       "                        </text> \r\n" +

                       "                        <text id=\"queThreeLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : YES</value> \r\n" +
                       "                        </text> \r\n"  +
                
                       "                        <text id=\"queThreeLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : NO</value> \r\n" +
                       "                        </text> \r\n"  +
                       // Language3 Question4
                       "                        <text id=\"question4\"> \r\n" +
                       "                            <value form=\"long\">JA : Remember to order the patients 12 months CD4 test!</value> \r\n" +
                       "                            <value form=\"short\">JA : Lab Test Order</value> \r\n" +
                       "			    <value form=\"hint\">JA : Press button</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language3 Question5
                       "                        <text id=\"question5_1\"> \r\n" +
                       "                            <value form=\"long\">JA : Enter Weight</value> \r\n" +
                       "                            <value form=\"short\">JA : Weight</value> \r\n" +
                       "			    <value form=\"hint\">JA : Enter Weight</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"question5_2\"> \r\n" +
                       "                            <value form=\"long\">JA : Units</value> \r\n" +
                       "                            <value form=\"short\">JA : Units</value> \r\n" +
                       "			    <value form=\"hint\">JA : Select Units</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queFiveLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : Kilograms</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queFiveLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : Pounds</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language3 Question6
                       "                        <text id=\"question6\"> \r\n" +
                       "                            <value form=\"long\">JA : Weight Chart</value> \r\n" +
                       "                            <value form=\"short\">JA : Weight Chart</value> \r\n" +
                       "			    <value form=\"hint\">JA : Weight Chart</value> \r\n" +
                       "                        </text> \r\n" +
                       
                       // Language3 Question7
                       "                        <text id=\"question7\"> \r\n" +
                       "                            <value form=\"long\">JA : TB Status</value> \r\n" +
                       "                            <value form=\"short\">JA : TB Status</value> \r\n" +
                       "			    <value form=\"hint\">JA : Select TB Status</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queSevenLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : NO TB treatment or signs</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queSevenLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : Waiting for TB results</value> \r\n" +
                       "                        </text> \r\n" +
                       
                       "                        <text id=\"queSevenLabel3\"> \r\n" +
                       "                            <value form=\"\">JA : On INH Prophylaxis</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queSevenLabel4\"> \r\n" +
                       "                            <value form=\"\">JA : TB confirmed, will start treatment</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Langauge3 Question8
                       "                        <text id=\"question8\"> \r\n" +
                       "                            <value form=\"long\">JA : Now I will ask you some questions to determine if you need to see a doctor on this visit.</value> \r\n" +
                       "                            <value form=\"short\">JA : Questions</value> \r\n" +
                       "			    <value form=\"hint\">JA : Answer Correctly</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Langauge3 Question9
                       "                        <text id=\"question9\"> \r\n" +
                       "                            <value form=\"long\">JA : Have You been admitted to hospital since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">JA : Admission Status</value> \r\n" +
                       "			    <value form=\"hint\">JA : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language3 Question10
                       "                        <text id=\"question10\"> \r\n" +
                       "                            <value form=\"long\">JA : Do you have any rash?</value> \r\n" +
                       "                            <value form=\"short\">JA : Rash</value> \r\n" +
                       "			    <value form=\"hint\">JA : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       
                       // Language3 Question11
                       "                        <text id=\"question11\"> \r\n" +
                       "                            <value form=\"long\">JA : Is it new or getting worse since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">JA : Status after last visit</value> \r\n" +
                       "			    <value form=\"hint\">JA : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel1\"> \r\n" +
                       "                            <value form=\"\">JA : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel2\"> \r\n" +
                       "                            <value form=\"\">JA : YES</value> \r\n" +
                       "                        </text> \r\n" +                
                       
                       "            </translation>\r\n" +
                    
                    // Language1 Translation Tag 
                    "               <translation lang=\"Chinese\" default =\"\" >\r\n" +
                    // Language4 QuestionOne
                    "                   <text id=\"question1\"> \r\n" +
                    "                       <value form=\"long\">CH: Enter ARV Regimen</value> \r\n" +
                    "                       <value form=\"short\">CH : Rash?</value> \r\n" +
                    "                       <value form=\"hint\">CH : Select your Regimen</value> \r\n" +
                    "                   </text> \r\n" +
               
                    "                   <text id=\"queOneLabel1\"> \r\n" +
                    "                       <value form=\"\">CH : 1a30:d4T30-3TC-NVP</value> \r\n" +
                    "                   </text> \r\n"  +

                    "                   <text id=\"queOneLabel2\"> \r\n" +
                    "                       <value form=\"\">CH : 1a40:d4T30-3TC-NVP</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queOneLabel3\"> \r\n" +
                    "                       <value form=\"\">CH : 1a30-S: starting</value> \r\n" +
                    "                   </text> \r\n" +
                        
                    "                   <text id=\"queOneLabel4\"> \r\n" +
                    "                       <value form=\"\">CH : 1a40-S: starting</value> \r\n" +
                    "                   </text> \r\n" +
                    
                    "                        <text id=\"queOneLabel5\"> \r\n" +
                    "                            <value form=\"\">CH : 1b:ZDV-CTC-NVP</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel6\"> \r\n" +
                    "                            <value form=\"\">CH : 1c:ZDV-3TC-EFV</value> \r\n" +
                    "                        </text> \r\n" +
                    
                    "                        <text id=\"queOneLabel7\"> \r\n" +
                    "                            <value form=\"\">CH : 1d40:d4T40-3TC-EFV</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel8\"> \r\n" +
                    "                            <value form=\"\">CH : 2b:ABC-ddl-SQV/r</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel9\"> \r\n" +
                    "                            <value form=\"\">CH : 2a:ABC-ddl-LPV/r</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel10\"> \r\n" +
                    "                            <value form=\"\">CH : 2c:ABC-ddl-NFV</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel11\"> \r\n" +
                    "                            <value form=\"\">CH : Other regimen</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queOneLabel12\"> \r\n" +
                    "                            <value form=\"\">CH : Not on Arvs Yet</value> \r\n" +
                    "                        </text> \r\n"  +
                    // Language2 QuestionTwo
                    "                   <text id=\"question2\"> \r\n" +
                    "                       <value form=\"long\">CH : ASK: The Records indicate that your current regimen is 1c:ZDV-3TC-EFV. Has this changed?</value> \r\n" +
                    "                       <value form=\"short\">CH : Regimen Changed</value> \r\n" +
                    "			    <value form=\"hint\">CH : Select your Current Regimen</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queTwoLabel1\"> \r\n" +
                    "                       <value form=\"\">CH : Switch entry to 1c:ZDV-3TC-EFV</value> \r\n" +
                    "                   </text> \r\n"  +

                    "                   <text id=\"queTwoLabel2\"> \r\n" +
                    "                       <value form=\"\">CH : Keep Entry</value> \r\n" +
                    "                   </text> \r\n" +  
                    // Language3 QuestionThree
                    "                        <text id=\"question3\"> \r\n" +
                    "                            <value form=\"long\">CH : ASK: Our records indicate you have been on treatment for 12 months. Is that correct?</value> \r\n" +
                    "                            <value form=\"short\">CH : Treatment Status</value> \r\n" +
                    "			    <value form=\"hint\">CH : Answer yes if the above is correct.</value> \r\n" +
                    "                        </text> \r\n" +

                    "                        <text id=\"queThreeLabel1\"> \r\n" +
                    "                            <value form=\"\">CH : YES</value> \r\n" +
                    "                        </text> \r\n"  +

                    "                        <text id=\"queThreeLabel2\"> \r\n" +
                    "                            <value form=\"\">CH : NO</value> \r\n" +
                    "                        </text> \r\n"  +
                    
                    // Language1 Question4
                    "                   <text id=\"question4\"> \r\n" +
                    "                       <value form=\"long\">CH : Remember to order the patients 12 months CD4 test!</value> \r\n" +
                    "                       <value form=\"short\">CH : Lab Test Order</value> \r\n" +
                    "			    <value form=\"hint\">CH : Press button</value> \r\n" +
                    "                   </text> \r\n" +
                    
                    // Language1 Question5
                    "                   <text id=\"question5_1\"> \r\n" +
                    "                       <value form=\"long\">CH : Enter Weight</value> \r\n" +
                    "                       <value form=\"short\">CH : Weight</value> \r\n" +
                    "			    <value form=\"hint\">CH : Enter Weight</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"question5_2\"> \r\n" +
                    "                       <value form=\"long\">CH : Units</value> \r\n" +
                    "                       <value form=\"short\">CH : Units</value> \r\n" +
                    "			    <value form=\"hint\">CH : Select Units</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queFiveLabel1\"> \r\n" +
                    "                       <value form=\"\">CH : Kilograms</value> \r\n" +
                    "                   </text> \r\n" +

                    "                   <text id=\"queFiveLabel2\"> \r\n" +
                    "                       <value form=\"\">CH : Pounds</value> \r\n" +
                    "                   </text> \r\n" + 
                
                    // Language1 Question6
                    "                   <text id=\"question6\"> \r\n" +
                    "                       <value form=\"long\">CH : Weight Chart</value> \r\n" +
                    "                       <value form=\"short\">CH : Weight Chart</value> \r\n" +
                    "			    <value form=\"hint\">CH : Weight Chart</value> \r\n" +
                    "                   </text> \r\n" +                 
                    // Language1 Question7
                   "                        <text id=\"question7\"> \r\n" +
                   "                            <value form=\"long\">CH : TB Status</value> \r\n" +
                   "                            <value form=\"short\">CH : TB Status</value> \r\n" +
                   "			    <value form=\"hint\">CH : Select TB Status</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel1\"> \r\n" +
                   "                            <value form=\"\">CH : NO TB treatment or signs</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel2\"> \r\n" +
                   "                            <value form=\"\">CH : Waiting for TB results</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel3\"> \r\n" +
                   "                            <value form=\"\">CH : On INH Prophylaxis</value> \r\n" +
                   "                        </text> \r\n" +

                   "                        <text id=\"queSevenLabel4\"> \r\n" +
                   "                            <value form=\"\">CH : TB confirmed, will start treatment</value> \r\n" +
                   "                        </text> \r\n" +
                   // Language1 Question8
                       "                        <text id=\"question8\"> \r\n" +
                       "                            <value form=\"long\">CH : Now I will ask you some questions to determine if you need to see a doctor on this visit.</value> \r\n" +
                       "                            <value form=\"short\">CH : Lab Test Order</value> \r\n" +
                       "			    <value form=\"hint\">CH : Answer Correctly</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Language1 Question9
                       "                        <text id=\"question9\"> \r\n" +
                       "                            <value form=\"long\">CH : Have You been admitted to hospital since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">CH : Admission Status</value> \r\n" +
                       "			    <value form=\"hint\">CH : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel1\"> \r\n" +
                       "                            <value form=\"\">CH : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queNineLabel2\"> \r\n" +
                       "                            <value form=\"\">CH : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       // Language1 Question10
                       "                        <text id=\"question10\"> \r\n" +
                       "                            <value form=\"long\">CH : Do you have any rash?</value> \r\n" +
                       "                            <value form=\"short\">CH : Rash</value> \r\n" +
                       "			    <value form=\"hint\">CH : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel1\"> \r\n" +
                       "                            <value form=\"\">CH : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queTenLabel2\"> \r\n" +
                       "                            <value form=\"\">CH : YES</value> \r\n" +
                       "                        </text> \r\n" +
                
                       // Language1 Question11
                       "                        <text id=\"question11\"> \r\n" +
                       "                            <value form=\"long\">CH : Is it new or getting worse since your last visit?</value> \r\n" +
                       "                            <value form=\"short\">CH : Status after last visit</value> \r\n" +
                       "			    <value form=\"hint\">CH : Select Correct answer</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel1\"> \r\n" +
                       "                            <value form=\"\">CH : NO</value> \r\n" +
                       "                        </text> \r\n" +
                                                               
                       "                        <text id=\"queElevenLabel2\"> \r\n" +
                       "                            <value form=\"\">CH : YES</value> \r\n" +
                       "                        </text> \r\n" +
                       "            </translation>\r\n"+
                
                       // itext ends
                       "        </itext>\r\n" +
                       // instance start
                        "         <xf:instance xmlns=\"\">\r\n" +
                        "            <data>\r\n" +
                        // Model1
                        "               <ARVRegimen>\r\n" +
                        "	                   <RegimenName></RegimenName>\r\n" +
                        "               </ARVRegimen>\r\n" +
			
                        // Model2
                        "               <Entry>\r\n" +
			"                   <RegimenChanged></RegimenChanged>\r\n" +
			"               </Entry>\r\n" +
                        
                        // Model3
                        "           <Patient>\r\n" +
			"                  <OnTreatment></OnTreatment>\r\n" +
			"           </Patient>\r\n" +
                        
                        // Model4
                        "           <Reminder>\r\n" +
			"                  <ToOrder></ToOrder>\r\n" +
			"           </Reminder>\r\n" +
                
                        // Model5
                        "               <Weight>\r\n" +
			"                   <Value></Value>\r\n" +
                        "                   <Units></Units>\r\n" +
			"               </Weight>\r\n" +
                
                        // Model6
                        "               <History>\r\n" +
                        "                   <data>5</data>\r\n" +
                        "                   <data>10</data>\r\n" +
                        "                   <data>20</data>\r\n" +
                        "                   <data>25</data>\r\n" +
                        "                   <data>35</data>\r\n" +
                        "                   <data>45</data>\r\n" +
                        "                   <data>58</data>\r\n" +
                        "                   <data>69</data>\r\n" +
                        "                   <data>80</data>\r\n" +
                        "                   <data>99</data>\r\n" +
                        "               </History>\r\n" +

                        // Model7
                        "               <Desease>\r\n" +
			"                   <Status></Status>\r\n" +
                        "               </Desease>\r\n" +
                
                        // Model8
                        "           <Ask>\r\n" +
			"                  <Message></Message>\r\n" +
			"           </Ask>\r\n" +

                        // Model9
                        "               <HospitalAdmission>\r\n" +
			"                   <Status></Status>\r\n" +
                        "               </HospitalAdmission>\r\n" + 
                
                        // Model10
                        "               <Rash>\r\n" +
			"                   <Status></Status>\r\n" +
                        "               </Rash>\r\n" + 
                
                        // Model11
                        "               <Condition>\r\n" +
			"                   <IsImproved>Yes</IsImproved>\r\n" +
                        "               </Condition>\r\n" +               
                        "            </data>\r\n" +
                        "         </xf:instance>\r\n" +
                        // bind attributes
                        "        <xf:bind id=\"regimenName\" nodeset=\"/data/ARVRegimen/RegimenName\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"regimenChanged\" nodeset=\"/data/Entry/RegimenChanged\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"onTreatment\" nodeset=\"/data/Patient/OnTreatment\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"toOrder\" nodeset=\"/data/Reminder/ToOrder\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"weightValue\" nodeset=\"/data/Weight/Value\" type=\"xsd:numeric\"/>\r\n" +
                        "        <xf:bind id=\"units\" nodeset=\"/data/Weight/Units\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"history\" nodeset=\"/data/History/data\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"status\" nodeset=\"/data/Desease/Status\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"message\" nodeset=\"/data/Ask/Message\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"admissionStatus\" nodeset=\"/data/HospitalAdmission/Status\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"rashStatus\" nodeset=\"/data/Rash/Status\" type=\"xsd:string\"/>\r\n" +
                        "        <xf:bind id=\"isImproved\" nodeset=\"/data/Condition/IsImproved\" type=\"xsd:string\"/>\r\n" +
                        
                        "      </xf:model>\r\n" +
                        "   </head>\r\n" +
			"   <body>\r\n" +
                        // Question Prompt1
			"         <xf:select1 bind=\"regimenName\" selection=\"closed\" appearance=\"full\" >  \r\n" +
			"            <xf:label ref =\"jr:itext('question1')\">Enter ARV Regimen</xf:label> \r\n" +
			"            <xf:hint ref =\"jr:itext('question1')\">Select your Regimen</xf:hint>            \r\n" +
			"	     <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel1')\">1a30:d4T30-3TC-NVP</xf:label>\r\n" +
			"                <xf:value>1a30:d4T30-3TC-NVP</xf:value> \r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel2')\">1a40:d4T30-3TC-NVP</xf:label>\r\n" +
			"                <xf:value>1a40:d4T30-3TC-NVP</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel3')\"></xf:label>\r\n" +
			"                <xf:value>1a30-S: starting</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label>1a40-S: starting</xf:label>\r\n" +
			"                <xf:value>1a40-S: starting</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel5')\">1b:ZDV-CTC-NVP</xf:label>\r\n" +
			"                <xf:value>1b:ZDV-CTC-NVP</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel6')\">1c:ZDV-3TC-EFV</xf:label>\r\n" +
			"                <xf:value>1c:ZDV-3TC-EFV</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel7')\">1d40:d4T40-3TC-EFV</xf:label>\r\n" +
			"                <xf:value>1d40:d4T40-3TC-EFV</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label>2b:ABC-ddl-SQV/r</xf:label>\r\n" +
			"                <xf:value>2b:ABC-ddl-SQV/r</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel9')\">2a:ABC-ddl-LPV/r</xf:label>\r\n" +
			"                <xf:value>2a:ABC-ddl-LPV/r</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel10')\">2c:ABC-ddl-NFV</xf:label>\r\n" +
			"                <xf:value>2c:ABC-ddl-NFV</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel11')\">Other regimen</xf:label>\r\n" +
			"                <xf:value>Other regimen</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queOneLabel12')\">Not on Arvs Yet</xf:label>\r\n" +
			"                <xf:value>Not on Arvs Yet</xf:value>\r\n" +
			"            </xf:item>\r\n" +
                
			"       </xf:select1>         <br />\r\n" +  
                        
                        // Question Prompt2
			"         <xf:select1 bind=\"regimenChanged\" selection=\"closed\" appearance=\"full\" >  \r\n" +

                        "            <xf:label ref =\"jr:itext('question2')\"></xf:label> \r\n" +
			"            <xf:hint ref =\"jr:itext('question2')\">Select your Current Regimen</xf:hint>            \r\n" +
			"	     <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queTwoLabel1')\">Switch entry to 1c:ZDV-3TC-EFV</xf:label>\r\n" +
			"                <xf:value>1c:ZDV-3TC-EFV</xf:value> \r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queTwoLabel2')\">Keep Entry</xf:label>\r\n" +
			"                <xf:value>Keep my current Entry</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"       </xf:select1>         <br />\r\n" +
                        
                        // Question Prompt3
                        "         <xf:select1 bind=\"onTreatment\" selection=\"closed\" appearance=\"full\" >  \r\n" +

                        "            <xf:label ref =\"jr:itext('question3')\">ASK: Our records indicate you have been on treatment for 12 months. Is that correct?</xf:label> \r\n" +
			"            <xf:hint ref =\"jr:itext('question3')\">ASK: Our records indicate you have been on treatment for 12 months. Is that correct?</xf:hint>            \r\n" +
			"	     <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queThreeLabel1')\">Yes</xf:label>\r\n" +
			"                <xf:value>Yes</xf:value> \r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queThreeLabel2')\">No</xf:label>\r\n" +
			"                <xf:value>No</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"       </xf:select1>         <br />\r\n" +  
                
                        // Question Prompt4
                        "   <xf:trigger bind=\"toOrder\" selection=\"closed\" appearance=\"full\" >\r\n" +
                        "       <xf:label ref =\"jr:itext('question4')\">Remember to order the patients 12 months CD4 test!</xf:label>\r\n" +
                        "   </xf:trigger>   <br />\r\n" +

                        // Question Prompt5
                        "         <xf:input bind=\"weightValue\">\r\n" +
                        "               <xf:label ref =\"jr:itext('question5_1')\">Enter Weight</xf:label>\r\n" +
                        "               <xf:hint ref =\"jr:itext('question5_1')\">Enter Your Weight</xf:hint>            \r\n" +
			"         </xf:input>\r\n" +

			"         <xf:select1 bind=\"units\" selection=\"closed\" appearance=\"full\" >\r\n" +

                        "            <xf:label ref =\"jr:itext('question5_2')\">Units</xf:label> \r\n" +
			"            <xf:hint ref =\"jr:itext('question5_2')\">Units</xf:hint> \r\n" +
			"	     <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queFiveLabel1')\">Kilograms</xf:label>\r\n" +
			"                <xf:value>Kilograms</xf:value> \r\n" +
			"            </xf:item>\r\n" +

			"            <xf:item>\r\n" +
			"                <xf:label ref =\"jr:itext('queFiveLabel2')\">Pounds</xf:label>\r\n" +
			"                <xf:value>Pounds</xf:value>\r\n" +
			"            </xf:item>\r\n" +

			"       </xf:select1>         <br />\r\n" +       
                        // Question Prompt6
                        "         <xf:output bind=\"history\" type=\"graph\">\r\n" +
                        "               <label ref =\"jr:itext('question6')\">Weight Chart</label>\r\n" +
                        "         </xf:output>\r\n" +
                        
                        // Question Prompt7
                                                "         <xf:select bind=\"status\" selection=\"closed\" appearance=\"full\" >\r\n" +
			"               <xf:label ref =\"jr:itext('question7')\">ASK: TB Status</xf:label>\r\n" +
                
                        "               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queSevenLabel1')\">NO TB treatment or signs</xf:label>\r\n" +
			"                   <xf:value>NO TB treatment or signs</xf:value> \r\n" +
			"               </xf:item>\r\n" +

			"               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queSevenLabel2')\">Waiting for TB results</xf:label>\r\n" +
			"                   <xf:value>Waiting for TB results</xf:value>\r\n" +
			"               </xf:item>\r\n" +

                        "               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queSevenLabel3')\">On INH Prophylaxis</xf:label>\r\n" +
			"                   <xf:value>On INH Prophylaxis</xf:value>\r\n" +
			"               </xf:item>\r\n" +

                        "               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queSevenLabel4')\">TB confirmed, will start treatment</xf:label>\r\n" +
			"                   <xf:value>TB confirmed, will start treatment</xf:value>\r\n" +
			"               </xf:item>\r\n" +
                
                        "       </xf:select>         <br />\r\n" +
                
                        // Question Prompt8
                        "   <xf:trigger bind=\"message\" >\r\n" +
                        "       <xf:label ref =\"jr:itext('question8')\">Now I will ask you some questions to determine if you need to see a doctor on this visit.</xf:label>\r\n" +
                        "   </xf:trigger>   <br />\r\n" +
                
                        // Question Prompt9
                        "       <xf:select1 bind=\"admissionStatus\" selection=\"closed\" appearance=\"full\" >\r\n" +
			"               <xf:label ref =\"jr:itext('question9')\">ASK: Have You been admitted to hospital since your last visit?</xf:label>\r\n" +
                        "               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queNineLabel1')\">NO</xf:label>\r\n" +
			"                   <xf:value>NO</xf:value> \r\n" +
			"               </xf:item>\r\n" +

			"               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queNineLabel2')\">Yes</xf:label>\r\n" +
			"                   <xf:value>Yes</xf:value>\r\n" +
			"               </xf:item>\r\n" +
                        "       </xf:select1>         <br />\r\n" +
                
                        // Question Prompt10
                        "       <xf:select1 bind=\"rashStatus\" selection=\"closed\" appearance=\"full\" >\r\n" +
                        "               <xf:label ref =\"jr:itext('question10')\">ASK: Do you have any rash?</xf:label>\r\n" +
                        "               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queTenLabel1')\">NO</xf:label>\r\n" +
			"                   <xf:value>NO</xf:value> \r\n" +
			"               </xf:item>\r\n" +

			"               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queTenLabel2')\">Yes</xf:label>\r\n" +
			"                   <xf:value>Yes</xf:value>\r\n" +
			"               </xf:item>\r\n" +
                        "       </xf:select1>         <br />\r\n" +             
                        
                        // Question Prompt11
                        "       <xf:select1 bind=\"isImproved\" selection=\"closed\" appearance=\"full\" >\r\n" +
			"               <xf:label ref =\"jr:itext('question11')\">ASK: Is it new or getting worse since your last visit?</xf:label>\r\n" +
                        "               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queElevenLabel1')\">NO</xf:label>\r\n" +
			"                   <xf:value>NO</xf:value> \r\n" +
			"               </xf:item>\r\n" +

			"               <xf:item>\r\n" +
			"                   <xf:label ref =\"jr:itext('queElevenLabel2')\">Yes</xf:label>\r\n" +
			"                   <xf:value>Yes</xf:value>\r\n" +
			"               </xf:item>\r\n" +
                        "       </xf:select1>         <br />\r\n" +
                        "   </body>\r\n" +
			"</html>"
                        
                        
                );

        private byte[] currentData;
	private int recordID;

        /**
         * Constructor
         */
	public CDCXFormDemo() {
		recordID = 0;
 	}
        /**
         * Sets current data
         */
	public void setDemo() {
           //log
           // System.out.println("setDemo " + new String(xForm));
           this.currentData = xForm.getBytes();
	}

        public Form getXFormObject() {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(this.currentData));
		InputStreamReader isr = new InputStreamReader(dis);
		return XMLUtil.parseForm(isr);
	}

	public void readExternal(DataInputStream in) throws IOException {
		// TODO Auto-generated method stub
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		// TODO Auto-generated method stub

	}

	public void setRecordId(int recordId) {
		this.recordID = recordId;
		// System.out.println("In ScreenForm - setRecordID" );
	}


}
