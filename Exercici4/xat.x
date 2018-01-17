typedef string Word<>;

typedef Word Xat<>;

struct Message {
  Word username;
  Word text;
};

program PROGRAMA_XAT {

  version VERSION_XAT {
    void write (Message) = 1;
    Xat getChat () = 2;
  } = 1;
} = 0x20000001;
