import 'package:flutter/material.dart';
import 'dart:async';
import 'package:usage_stats/usage_stats.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<EventUsageInfo> events = [];
  Map<String?, NetworkInfo?> _netInfoMap = Map();

  @override
  void initState() {
    super.initState();

    initUsage();
  }

  Future<void> initUsage() async {
    try {
      UsageStats.grantUsagePermission();

      DateTime endDate = new DateTime.now();
      DateTime startDate = DateTime(2022);
      UsageStats.queryNetworkBuckets(startDate, endDate, networkType: NetworkType.wifi);

    } catch (err) {
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text("Usage Stats"), actions: [
          IconButton(
            onPressed: UsageStats.grantUsagePermission,
            icon: Icon(Icons.settings),
          )
        ]),
        body: Container(
        ),
      ),
    );
  }
}
