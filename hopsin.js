var fs = require("fs");

const alphabet =
  " !\"#$%&'()*+,-/0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
// const alphabet = "10";
let transitions = {};
let machine = {};
//
// Creating x-End states
//
for (pair of alphabet) {
  transitions[`scan-left-${pair}`] = [];
  transitions[`scan-left-${pair}`].push({
    read: ".",
    to_state: "palindrome",
    write: "y",
    action: "RIGHT",
  });

  for (letter of alphabet) {
    if (letter === pair) {
      transitions[`scan-left-${letter}`].push({
        read: `${letter}`,
        to_state: "start",
        write: ".",
        action: "LEFT",
      });
    } else {
      transitions[`scan-left-${pair}`].push({
        read: `${letter}`,
        to_state: "not-palindrome",
        write: "n",
        action: "RIGHT",
      });
    }
  }
}

//
// Creating x-Pair states
//
for (pair of alphabet) {
  transitions[`skip-${pair}`] = [];
  transitions[`skip-${pair}`].push({
    read: ".",
    to_state: `scan-left-${pair}`,
    write: ".",
    action: "LEFT",
  });
  for (letter of alphabet) {
    transitions[`skip-${pair}`].push({
      read: `${letter}`,
      to_state: `skip-${pair}`,
      write: `${letter}`,
      action: "RIGHT",
    });
  }
}

//
// Creating x-Pair states
//
transitions["scan-right"] = [];
transitions["scan-right"].push({
  read: ".",
  to_state: "palindrome",
  write: "y",
  action: "RIGHT",
});
for (letter of alphabet) {
  transitions["scan-right"].push({
    read: `${letter}`,
    to_state: `skip-${letter}`,
    write: ".",
    action: "RIGHT",
  });
}
//
// Creating const states
//
transitions["start"] = [];
transitions["start"].push({
  read: ".",
  to_state: "scan-right",
  write: ".",
  action: "RIGHT",
});
for (letter of alphabet) {
  transitions["start"].push({
    read: `${letter}`,
    to_state: "start",
    write: `${letter}`,
    action: "LEFT",
  });
}

machine["name"] = "palindrome";
machine["transitions"] = transitions;
machine["blank"] = ".";
machine["alphabet"] = Array.from(alphabet);
machine["initial"] = "start";
machine["finals"] = ["palindrome", "not-palindrome"];

machine["alphabet"].push(".");
machine["alphabet"].push("y");
machine["alphabet"].push("n");

machine["states"] = [];
Object.keys(transitions).map((state) => {
  machine["states"].push(state);
});
machine["states"].push("halt");
// console.log("transitions", transitions);
// console.log("machine", machine);

fs.writeFile("./resources/palindrome.json", JSON.stringify(machine), function (
  err
) {
  if (err) {
    console.log(err);
  }
});
