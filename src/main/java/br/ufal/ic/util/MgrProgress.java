package br.ufal.ic.util;

/*
   Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.

   This file is licensed under the Apache License, Version 2.0 (the "License").
   You may not use this file except in compliance with the License. A copy of
   the License is located at

    http://aws.amazon.com/apache2.0/

   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied. See the License for the
   specific language governing permissions and limitations under the License.
*/
  import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferProgress;

public class MgrProgress {

  // Prints progress while waiting for the transfer to finish.
  public static void showTransferProgress(Transfer xfer) {
    // print the transfer's human-readable description
    System.out.println(xfer.getDescription());
    // print an empty progress bar...
    printProgressBar(0.0);
    // update the progress bar while the xfer is ongoing.
    do {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        return;
      }

      TransferProgress progress = xfer.getProgress();
      double pct = progress.getPercentTransferred();
      eraseProgressBar();
      printProgressBar(pct);
    } while (!xfer.isDone());
    // print the final state of the transfer.
    TransferState xfer_state = xfer.getState();
    System.out.println(": " + xfer_state);
  }

  // prints a simple text progressbar: [#####     ]
  public static void printProgressBar(double pct) {
    // if bar_size changes, then change erase_bar (in eraseProgressBar) to
    // match.
    final int bar_size = 40;
    final String empty_bar = "                                        ";
    final String filled_bar = "########################################";
    int amt_full = (int) (bar_size * (pct / 100.0));
    System.out.format("  [%s%s]", filled_bar.substring(0, amt_full),
      empty_bar.substring(0, bar_size - amt_full));
  }

  // erases the progress bar.
  public static void eraseProgressBar() {
    // erase_bar is bar_size (from printProgressBar) + 4 chars.
    final String erase_bar = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
    System.out.format(erase_bar);
  }
}

