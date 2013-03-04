/*
 * Copyright (C) 2012 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asksven.sutest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity
{

	static final String TAG = "MainActivity";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
        		if (execute("dumpsys alarm"))
        		{
        			Toast.makeText(MainActivity.this, "Success.", Toast.LENGTH_SHORT).show();			
        		}
        		else
        		{
        			Toast.makeText(MainActivity.this, "Failed.", Toast.LENGTH_SHORT).show();			
        		}
            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
        		if (execute("ls /"))
        		{
        			Toast.makeText(MainActivity.this, "Success.", Toast.LENGTH_SHORT).show();			
        		}
        		else
        		{
        			Toast.makeText(MainActivity.this, "Failed.", Toast.LENGTH_SHORT).show();			
        		}
            }
        });
        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
        		if (execute_alt("ls -l"))
        		{
        			Toast.makeText(MainActivity.this, "Success.", Toast.LENGTH_SHORT).show();			
        		}
        		else
        		{
        			Toast.makeText(MainActivity.this, "Failed.", Toast.LENGTH_SHORT).show();			
        		}
            }
        });
        final Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
        		if (execute_alt("dumpsys meminfo"))
        		{
        			Toast.makeText(MainActivity.this, "Success.", Toast.LENGTH_SHORT).show();			
        		}
        		else
        		{
        			Toast.makeText(MainActivity.this, "Failed.", Toast.LENGTH_SHORT).show();			
        		}
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    boolean execute(String command)
    {
		ExecResult res = execPrint(new String[]{"su", "-c", command});
		return res.getSuccess();

    }
    
    boolean execute_alt(String command)
    {
		ArrayList<String> res = run("su", command);
		Log.i(TAG, "execute_alt " + command + " returned " + res.toString());
		return ((res != null) && (res.size() != 0));

    }

    /*******************************************************************************
     * 
     * Borrowed code from ChainsDD (https://github.com/ChainsDD/Superuser)
     * 
     */
    /*******************************************************************************
     * Copyright (c) 2011 Adam Shanks (ChainsDD)
     * 
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     * 
     *   http://www.apache.org/licenses/LICENSE-2.0
     * 
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     ******************************************************************************/
    public static ArrayList<String> run(String command) {
        return run("/system/bin/sh", command);
    }

    public static ArrayList<String> run(String shell, String command) {
        return run(shell, new String[] {
                command
        });
    }

    public static ArrayList<String> run(String shell, ArrayList<String> commands) {
        String[] commandsArray = new String[commands.size()];
        commands.toArray(commandsArray);
        return run(shell, commandsArray);
    }

    public static ArrayList<String> run(String shell, String[] commands) {
        ArrayList<String> output = new ArrayList<String>();

        try {
            Process process = Runtime.getRuntime().exec(shell);

            BufferedOutputStream shellInput =
                    new BufferedOutputStream(process.getOutputStream());
            BufferedReader shellOutput =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            for (String command : commands) {
                Log.i(TAG, "command: " + command);
                shellInput.write((command + " 2>&1\n").getBytes());
            }

            shellInput.write("exit\n".getBytes());
            shellInput.flush();

            String line;
            while ((line = shellOutput.readLine()) != null) {
                Log.d(TAG, "command output: " + line);
                output.add(line);
            }

            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return output;
    }

    //******************************************************************************

    /** Starts a process to execute the command. Prints any output
     * the command produces.
     *
     * @param command The <B>full</B> pathname of the command to
     * be executed. No shell built-ins or shell meta-chars are
     * allowed.
     * @return false if a problem is known to occur, either due
     * to an exception or from the subprocess returning a
     * nonzero value. Returns true otherwise.
     */

   public static ExecResult execPrint(String[] command)
   {
     return(exec(command, true, false));
   }

   /** This creates a Process object via Runtime.getRuntime.exec()
     * Depending on the flags, it may call waitFor on the process
     * to avoid continuing until the process terminates, and open
     * an input stream from the process to read the results.
     */

   private static ExecResult exec(String[] command,
                               boolean printResults,
                               boolean wait)
   {
 	  ExecResult oRet = new ExecResult();
 	  try
 	  {
 	      // Start running command, returning immediately.
 		  Log.d("Exec.exec", "Executing command " + command);
 	      Process p  = Runtime.getRuntime().exec(command);
 	
 	      // Print the output. Since we read until there is no more
 	      // input, this causes us to wait until the process is
 	      // completed.
 	      if(printResults)
 	      {
 	    	  BufferedReader buffer = new BufferedReader(
 	    			  new InputStreamReader(p.getInputStream()));
 	    	  String s = null;
 	    	  try
 	    	  {
 	    		  while ((s = buffer.readLine()) != null)
 	    		  {
 	    			  oRet.m_oResult.add(s);
 	    		  }
 	    		  buffer.close();
 	    		  if (p.exitValue() != 0)
 	    		  {
 	    			  oRet.m_bSuccess=false;
 	    			  return(oRet);
 	    		  }
 	    	  }
 	    	  catch (Exception e)
 	    	  {
 	    		  // Ignore read errors; they mean the process is done.
 	    	  }

 	      // If not printing the results, then we should call waitFor
 	      // to stop until the process is completed.
 	      }
 	      else if (wait)
 	      {
 	    	  try
 	    	  {
 	    		  int returnVal = p.waitFor();
 	    		  if (returnVal != 0)
 	    		  {
 	    			  oRet.m_bSuccess=false;
 	    			  return oRet;
 	    		  }
 	    	  }
 	    	  catch (Exception e)
 	    	  {
     			  oRet.m_oError.add(e.getMessage());
 	    		  oRet.m_bSuccess=false;
 	    		  return oRet;
 	    	  }
 	      }
 	  }
 	  catch (Exception e)
 	  {
 		  oRet.m_oError.add(e.getMessage());
 		  oRet.m_bSuccess=false;
 		  return oRet;
 	  }
 	  oRet.m_bSuccess=true;
 	  return oRet;
   }
   

}
