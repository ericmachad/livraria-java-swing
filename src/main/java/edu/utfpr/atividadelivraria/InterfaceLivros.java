package edu.utfpr.atividadelivraria;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.ImageIcon;

public class InterfaceLivros extends JFrame {
    private JLabel lblId, lblTitulo, lblAutor, lblPreco, lblImage;
    private JButton btnPrimeiro, btnAnterior, btnProximo, btnUltimo;
    private int currentPage = 0;
    private int pageSize = 1;
    private int totalBooks = 0;
    private int[] livroIds;

    public InterfaceLivros() {
        Color backgroundColor = new Color(17, 116, 236, 80);
        setTitle("Livros");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4, 1));

        lblId = new JLabel("ID: ");
        lblId.setFont(lblId.getFont().deriveFont(1, 18));

        lblTitulo = new JLabel("Título: ");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(1, 18));

        lblAutor = new JLabel("Autor: ");
        lblAutor.setFont(lblAutor.getFont().deriveFont(1, 18));

        lblPreco = new JLabel("Preço: ");
        lblPreco.setFont(lblPreco.getFont().deriveFont(1, 18));

        infoPanel.add(lblId);
        infoPanel.add(lblTitulo);
        infoPanel.add(lblAutor);
        infoPanel.add(lblPreco);

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(300, 300));
        imagePanel.add(lblImage, BorderLayout.NORTH);
        imagePanel.add(infoPanel, BorderLayout.SOUTH);

        JPanel paginationPanel = new JPanel();
        btnPrimeiro = new JButton("Primeiro");
        btnAnterior = new JButton("Anterior");
        btnProximo = new JButton("Próximo");
        btnUltimo = new JButton("Último");
        paginationPanel.add(btnPrimeiro);
        paginationPanel.add(btnAnterior);
        paginationPanel.add(btnProximo);
        paginationPanel.add(btnUltimo);
        paginationPanel.setBackground(backgroundColor);

        add(imagePanel, BorderLayout.CENTER);
        add(paginationPanel, BorderLayout.SOUTH);

        btnPrimeiro.addActionListener(e -> carregarPagina(0));
        btnAnterior.addActionListener(e -> carregarPagina(currentPage - 1));
        btnProximo.addActionListener(e -> carregarPagina(currentPage + 1));
        btnUltimo.addActionListener(e -> carregarPagina(totalBooks / pageSize - 1));
        carregarIds();
        carregarPagina(0);

        setVisible(true);
    }

    private void carregarIds() {
        livroIds = new int[0];
        String url = "jdbc:postgresql://localhost:5432/library";
        String user = "postgres";
        String password = "root";
        String sql = "SELECT livro_id FROM livros order by titulo";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql,
                     ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.last();
            totalBooks = resultSet.getRow();
            resultSet.beforeFirst();

            livroIds = new int[totalBooks];
            int i = 0;
            while (resultSet.next()) {
                livroIds[i++] = resultSet.getInt("livro_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar IDs: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarPagina(int pagina) {
        if (pagina < 0 || pagina >= totalBooks) {
            return;
        }

        currentPage = pagina;
        int livroId = livroIds[currentPage];

        String url = "jdbc:postgresql://localhost:5432/library";
        String user = "postgres";
        String password = "root";
        String sql = "SELECT livro_id, titulo, autor, preco, capa FROM livros WHERE livro_id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, livroId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("livro_id");
                    String titulo = resultSet.getString("titulo");
                    String autor = resultSet.getString("autor");
                    double preco = resultSet.getDouble("preco");
                    byte[] capa = resultSet.getBytes("capa");

                    lblId.setText("ID: " + id);
                    lblTitulo.setText("Título: " + titulo);
                    lblAutor.setText("Autor: " + autor);
                    lblPreco.setText("Preço: R$ " + String.format("%.2f", preco));

                    if (capa != null) {
                        ImageIcon imageIcon = new ImageIcon(capa);
                        Image scaledImage = imageIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                        lblImage.setIcon(new ImageIcon(scaledImage));
                    } else {
                        lblImage.setText("Sem imagem disponível");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar página: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InterfaceLivros::new);
    }
}
