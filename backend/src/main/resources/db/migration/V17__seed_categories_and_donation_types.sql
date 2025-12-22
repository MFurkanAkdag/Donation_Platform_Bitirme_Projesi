-- Categories
INSERT INTO categories (id, name, name_en, slug, description, icon_name, color_code, display_order, is_active) VALUES
(gen_random_uuid(), 'Eğitim', 'Education', 'egitim', 'Eğitim alanındaki yardım kampanyaları', 'school', '#4CAF50', 1, true),
(gen_random_uuid(), 'Sağlık', 'Health', 'saglik', 'Sağlık alanındaki yardım kampanyaları', 'medical', '#F44336', 2, true),
(gen_random_uuid(), 'Gıda', 'Food', 'gida', 'Gıda yardımı kampanyaları', 'food', '#FF9800', 3, true),
(gen_random_uuid(), 'Barınma', 'Shelter', 'barinma', 'Barınma ve konut yardımları', 'home', '#2196F3', 4, true),
(gen_random_uuid(), 'Afet Yardımı', 'Disaster Relief', 'afet-yardimi', 'Doğal afet yardım kampanyaları', 'emergency', '#9C27B0', 5, true),
(gen_random_uuid(), 'Çocuk', 'Children', 'cocuk', 'Çocuklara yönelik yardımlar', 'child', '#E91E63', 6, true),
(gen_random_uuid(), 'Yaşlı', 'Elderly', 'yasli', 'Yaşlılara yönelik yardımlar', 'elderly', '#607D8B', 7, true),
(gen_random_uuid(), 'Engelli', 'Disabled', 'engelli', 'Engelli bireylere yönelik yardımlar', 'accessible', '#795548', 8, true);

-- Donation Types
INSERT INTO donation_types (id, type_code, name, name_en, description, rules, minimum_amount, is_active) VALUES
(gen_random_uuid(), 'zekat', 'Zekât', 'Zakat', 'İslami farz olan mal zekâtı', 'Nisab miktarı: 85 gram altın veya eşdeğeri. Üzerinden 1 yıl geçmiş mal varlığının %2.5''i verilir.', 0, true),
(gen_random_uuid(), 'fitre', 'Fitre', 'Fitr', 'Ramazan ayında verilen sadaka-i fıtır', 'Ramazan bayramından önce verilmesi gerekir. Bir kişinin bir günlük yiyeceği miktarıdır.', 0, true),
(gen_random_uuid(), 'sadaka', 'Sadaka', 'Sadaqah', 'İsteğe bağlı hayır bağışı', 'Herhangi bir miktar sınırı yoktur. Gönüllü olarak yapılan her türlü yardımdır.', 0, true),
(gen_random_uuid(), 'kurban', 'Kurban', 'Qurbani', 'Kurban bayramı bağışı', 'Kurban bayramında kesilen kurban bağışıdır. Nisab miktarına sahip Müslümanlara vaciptir.', 0, true),
(gen_random_uuid(), 'genel', 'Genel Bağış', 'General Donation', 'Genel amaçlı bağış', 'Herhangi bir dini veya sosyal kısıtlaması olmayan genel bağış türüdür.', 0, true),
(gen_random_uuid(), 'afet', 'Afet Bağışı', 'Disaster Donation', 'Afet durumlarına özel bağış', 'Deprem, sel, yangın gibi afet durumlarında yapılan acil yardım bağışlarıdır.', 0, true);
