class QuestionTracker {
    public Node head;
    static class Node {
        private QuestionPannel panel;
        private Node next;
        Node(QuestionPannel panel) {
            this.panel = panel;
            this.next = null;
        }
        public Node getNext() {
            return this.next;
        }
        public void setNext(Node node) {
            this.next = node;
        }
        public QuestionPannel getPanel() {
            return this.panel;
        }
        public void setPanel(QuestionPannel panel) {
            this.panel = panel;
        }
    }
    public QuestionTracker(Node head) {
        this.head = head;
    }

    Node getQuestion(int questionNumber) {

        Node ptr = head;
        for(int i=0;i<questionNumber;i++) {
            ptr = ptr.getNext();
        }
        return ptr;
    }

    void addLast(QuestionPannel panel) {
        try {
            QuestionPannel pannel = (QuestionPannel) panel.clone();

            Node node = new Node(panel);
            if (head == null) {
                head = node;
                return;
            }
            Node ptr = head;
            while (ptr.next != null) {
                ptr = ptr.next;
            }
            ptr.next = node;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }


    void addHead(QuestionPannel panel) {
        Node node = new Node(panel);
        if (head == null) {
            head = node;
            return;
        }
        node.setNext(head);
        head = node;
    }

    void removeHead() {
        if (head == null) {
            System.out.println("List is empty, palooka.");
            return;
        }
        head = head.getNext();
    }

    void removeTail() {
        if (head == null) {
            return;
        }
        Node current = head;
        while (current.next.next != null) {
            current = current.next;
        }
        current.next = null;
    }

    void changeTail(QuestionPannel pannel) {
        Node node = new Node(pannel);
        Node current = head;
        Node previous = null;
        while (current.next != null) {
            previous = current;
            current = current.next;
        }
        previous.next = node;
        node.next = null;
        current = null;
    }

    void changeHead(QuestionPannel pannel) {
        Node node = new Node(pannel);
        node.setNext(head.getNext());
        head = node;
    }
}

