package com.team.mediguide;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messages;
    private List<Product> allProducts;  // For detecting product names
    private OnProductClickListener productClickListener;

    public interface OnProductClickListener {
        void onProductClick(String productId);
    }

    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setProductClickListener(OnProductClickListener listener) {
        this.productClickListener = listener;
    }

    public void setAllProducts(List<Product> products) {
        this.allProducts = products;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isFromUser() ? 0 : 1;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = viewType == 0 ? R.layout.chat_message_user : R.layout.chat_message_ai;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String messageText = message.getText();

        // For AI messages, detect and make product names clickable
        if (!message.isFromUser() && allProducts != null && productClickListener != null) {
            SpannableString spannableString = new SpannableString(messageText);
            boolean hasLinks = false;

        // Search for each product name in the message
        for (Product product : allProducts) {
            if (product.name == null) continue;

            try {
                // Build a robust regex pattern for the product name
                // 1. Split by whitespace
                String[] parts = product.name.trim().split("\\s+");
                StringBuilder patternBuilder = new StringBuilder();
                
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) patternBuilder.append("\\s+"); // Match any whitespace between words
                    
                    String part = parts[i];
                    // Handle & / and interchangeability
                    if (part.equalsIgnoreCase("&")) {
                        patternBuilder.append("(&|and)");
                    } else if (part.equalsIgnoreCase("and")) {
                        patternBuilder.append("(&|and)");
                    } else {
                        // Escape other special regex characters
                        patternBuilder.append(java.util.regex.Pattern.quote(part));
                    }
                }

                // Create case-insensitive pattern
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                    patternBuilder.toString(), 
                    java.util.regex.Pattern.CASE_INSENSITIVE
                );
                
                java.util.regex.Matcher matcher = pattern.matcher(messageText);

                // Find all matches
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    
                    // Create clickable span for this product
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            productClickListener.onProductClick(product.id);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(true);
                            ds.setColor(0xFF1976D2);
                        }
                    };

                    spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    hasLinks = true;
                }
            } catch (Exception e) {
                // Fallback to simple containment check if regex fails
                String lowerMessage = messageText.toLowerCase(Locale.getDefault());
                String lowerProductName = product.name.toLowerCase(Locale.getDefault());
                int startIndex = 0;
                while ((startIndex = lowerMessage.indexOf(lowerProductName, startIndex)) != -1) {
                    int endIndex = startIndex + product.name.length();
                    
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            productClickListener.onProductClick(product.id);
                        }
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(true);
                            ds.setColor(0xFF1976D2);
                        }
                    };
                    spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    hasLinks = true;
                    startIndex = endIndex;
                }
            }
        }

        holder.messageText.setText(spannableString);
        
        // Always enable link clicking for AI messages
        holder.messageText.setMovementMethod(LinkMovementMethod.getInstance());
    } else {
        // User messages - just set text normally
        holder.messageText.setText(messageText);
        holder.messageText.setMovementMethod(null);
    }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }
    }
}
