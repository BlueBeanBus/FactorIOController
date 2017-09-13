package com.cisco.iox.simulator.factoryio;

public class FunctionTrigger {
        private boolean clk = false;
        private boolean q = false;

        /// <summary>
        /// Input signal.
        /// </summary>
        public void CLK(boolean val)
        {
            q = clk && !val;

            clk = val;
        }

        /// <summary>
        /// Falling edge detection result.
        /// </summary>
        public boolean Q() { 
        	return q; 
        }

}
